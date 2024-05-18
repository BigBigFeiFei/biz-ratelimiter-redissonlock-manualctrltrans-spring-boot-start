package com.zlf.annotation;

import cn.hutool.core.lang.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


@Slf4j
@Aspect
@Component
public class BizIdempotentManualCtrlTransAspect {

    @Autowired
    private TransactionDefinition transactionDefinition;

    @Autowired
    private DataSourceTransactionManager transactionManager;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisTemplate<String, Long> bizRedisTemplateLimit;

    private static final List<String> KEY_FORMAT_MATCHS = new ArrayList<>();

    static {
        KEY_FORMAT_MATCHS.add("%s");
    }

    @Pointcut("@annotation(com.zlf.annotation.BizIdempotentManualCtrlTransLimiterAnno)")
    public void bizIdempotentManualCtrlTransPoint() {

    }

    @Around("bizIdempotentManualCtrlTransPoint()")
    public Object deal(ProceedingJoinPoint pjp) throws Throwable {
        //当前线程名
        String threadName = Thread.currentThread().getName();
        log.info("-------------BizIdempotentManualCtrlTransLimiterAnno开始执行-----线程{}-----------", threadName);
        //获取参数列表
        Object[] objs = pjp.getArgs();
        String key = null;
        String redisLimitKey = null;
        String message = "";
        BizIdempotentManualCtrlTransLimiterAnno annotation = null;
        try {
            //注解加上的public方法的第一个参数就是key,只支持改参数为String类型
            key = (String) objs[0];
            if (Objects.isNull(key)) {
                return pjp.proceed();
            }
            //获取该注解的实例对象
            annotation = ((MethodSignature) pjp.getSignature()).
                    getMethod().getAnnotation(BizIdempotentManualCtrlTransLimiterAnno.class);
            //是否开启RedissonLock
            boolean openRedissonLock = annotation.isOpenRedissonLock();
            boolean openManualCtrlTrans = annotation.isOpenManualCtrlTrans();
            boolean openLimit = annotation.isOpenLimit();
            boolean bothFlag = openRedissonLock && openManualCtrlTrans;
            if (openLimit) {
                int errorCount = annotation.limitMaxErrorCount();
                String limitRedisKey = annotation.limitRedisKeyPrefix();
                redisLimitKey = String.format(limitRedisKey, key);
                TimeUnit timeUnit = annotation.limitRedisKeyTimeUnit();
                this.checkFailCount(redisLimitKey, errorCount, timeUnit);
                if (!openRedissonLock && !openManualCtrlTrans) {
                    return pjp.proceed();
                }
            }
            if (!bothFlag) {
                if (openRedissonLock) {
                    key = checkKeyFormatMatch(annotation, key);
                    RLock lock = redissonClient.getLock(key);
                    try {
                        Tuple lockAnnoParamsTuple = this.getLockAnnoParams(annotation);
                        long t = lockAnnoParamsTuple.get(0);
                        TimeUnit uint = lockAnnoParamsTuple.get(1);
                        if (lock.tryLock(t, uint)) {
                            return pjp.proceed();
                        }
                    } catch (Exception e) {
                        log.error("-------------BizIdempotentManualCtrlTransLimiterAnno锁异常ex:{}-----线程{}-----------", ExceptionUtils.getMessage(e), threadName);
                        throw new RuntimeException(ExceptionUtils.getMessage(e));
                    } finally {
                        if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                            lock.unlock();
                            log.info("-------------BizIdempotentManualCtrlTransLimiterAnno释放锁成功-----线程{}-----------", threadName);
                        }
                    }
                }
                if (openManualCtrlTrans) {
                    TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
                    try {
                        Object proceed = pjp.proceed();
                        transactionManager.commit(transactionStatus);
                        return proceed;
                    } catch (Exception e) {
                        transactionManager.rollback(transactionStatus);
                        log.info("-------------BizIdempotentManualCtrlTransLimiterAnno执行异常事务回滚1-----线程{}-----------", threadName);
                        throw new RuntimeException(ExceptionUtils.getMessage(e));
                    }
                }
            }
            if (bothFlag) {
                key = checkKeyFormatMatch(annotation, key);
                RLock lock = redissonClient.getLock(key);
                TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
                try {
                    Tuple lockAnnoParamsTuple = this.getLockAnnoParams(annotation);
                    long t = lockAnnoParamsTuple.get(0);
                    TimeUnit uint = lockAnnoParamsTuple.get(1);
                    if (lock.tryLock(t, uint)) {
                        Object proceed = pjp.proceed();
                        transactionManager.commit(transactionStatus);
                        return proceed;
                    }
                } catch (Exception e) {
                    log.error("-------------BizIdempotentManualCtrlTransLimiterAnno处理异常ex:{}-----线程{}-----------", ExceptionUtils.getMessage(e), threadName);
                    transactionManager.rollback(transactionStatus);
                    log.info("-------------BizIdempotentManualCtrlTransLimiterAnno执行异常事务回滚2-----线程{}-----------", threadName);
                    throw new RuntimeException(ExceptionUtils.getMessage(e));
                } finally {
                    if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                        lock.unlock();
                        log.info("-------------BizIdempotentManualCtrlTransLimiterAnno释放锁成功2-----线程{}-----------", threadName);
                    }
                }
            }
        } catch (Exception e) {
            message = ExceptionUtils.getMessage(e);
            String stackTrace = ExceptionUtils.getStackTrace(e);
            String limitExType = annotation.limitTye();
            log.error("------------BizIdempotentManualCtrlTransLimiterAnno-------msg:{},stackTrace:{},limitExType:{}-------", message, stackTrace, limitExType);
            boolean openLimit = annotation.isOpenLimit();
            TimeUnit timeUnit = annotation.limitRedisKeyTimeUnit();
            long limitRedisKeyExpireTime = annotation.limitRedisKeyExpireTime();
            String expContent = annotation.expContent();
            if (openLimit) {
                if (StringUtils.isNotBlank(message) && StringUtils.isNotBlank(expContent) && message.indexOf(expContent) != -1) {
                    log.error("------------BizIdempotentManualCtrlTransLimiterAnno-------openLimit:{},message:{},expContent:{}-------", openLimit, message, expContent);
                    if (!bizRedisTemplateLimit.hasKey(redisLimitKey)) {
                        bizRedisTemplateLimit.opsForValue().set(redisLimitKey, 1L, limitRedisKeyExpireTime, timeUnit);
                    } else {
                        bizRedisTemplateLimit.opsForValue().increment(redisLimitKey);
                    }
                } else if (stackTrace.indexOf(limitExType) != -1 && StringUtils.isBlank(expContent)) {
                    log.error("------------BizIdempotentManualCtrlTransLimiterAnno-------openLimit:{},stackTrace:{},expContent:{}-------", openLimit, stackTrace, limitExType);
                    if (!bizRedisTemplateLimit.hasKey(redisLimitKey)) {
                        bizRedisTemplateLimit.opsForValue().set(redisLimitKey, 1L, limitRedisKeyExpireTime, timeUnit);
                    } else {
                        bizRedisTemplateLimit.opsForValue().increment(redisLimitKey);
                    }
                } else {
                    if (!bizRedisTemplateLimit.hasKey(redisLimitKey)) {
                        bizRedisTemplateLimit.opsForValue().set(redisLimitKey, 1L, limitRedisKeyExpireTime, timeUnit);
                    } else {
                        bizRedisTemplateLimit.opsForValue().increment(redisLimitKey);
                    }
                }
            }
            log.error("-------------BizIdempotentManualCtrlTransLimiterAnno开始异常ex:{}-----线程{}-----------", ExceptionUtils.getMessage(e), threadName);
        }
        throw new RuntimeException(message.replaceAll("RuntimeException", "").replaceAll("Exception", "").replaceAll(":", "").replaceAll(" ", ""));
    }

    private void checkFailCount(String key, long errorCount, TimeUnit timeUnit) {
        boolean isExistKey = bizRedisTemplateLimit.hasKey(key);
        if (isExistKey) {
            Long count = (Long) bizRedisTemplateLimit.opsForValue().get(key);
            log.info("=========BizIdempotentManualCtrlTransLimiterAnno=====key:{}=======failCount:{}=========", key, count);
            if (Objects.nonNull(count) && count > errorCount) {
                Long expire = bizRedisTemplateLimit.getExpire(key, timeUnit);
                String unitStr = "";
                if (timeUnit.equals(TimeUnit.DAYS)) {
                    unitStr = "天";
                } else if (timeUnit.equals(TimeUnit.HOURS)) {
                    unitStr = "小时";
                } else if (timeUnit.equals(TimeUnit.MINUTES)) {
                    unitStr = "分钟";
                } else if (timeUnit.equals(TimeUnit.SECONDS)) {
                    unitStr = "秒钟";
                } else if (timeUnit.equals(TimeUnit.MILLISECONDS)) {
                    unitStr = "毫秒";
                }
                log.error("BizIdempotentManualCtrlTransLimiterAnno异常次数限制,错误次数:{}", errorCount);
                throw new RuntimeException("请求异常,请" + expire + unitStr + "后重试!");
            }
        }
    }

    private Tuple getLockAnnoParams(BizIdempotentManualCtrlTransLimiterAnno annotation) {
        long t = annotation.lockTime();
        TimeUnit unit = annotation.lockTimeUnit();
        return new Tuple(t, unit);
    }

    private String checkKeyFormatMatch(BizIdempotentManualCtrlTransLimiterAnno annotation, String key) {
        String keyFormat = annotation.keyFormat();
        if (StringUtils.isNotBlank(keyFormat)) {
            if (!KEY_FORMAT_MATCHS.contains(keyFormat)) {
                throw new RuntimeException("注解key格式匹配有误!");
            }
            key = String.format(keyFormat, key);
        }
        return key;
    }

}
