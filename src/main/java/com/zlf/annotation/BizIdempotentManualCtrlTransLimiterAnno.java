package com.zlf.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @author zlf
 */
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BizIdempotentManualCtrlTransLimiterAnno {


    /**
     * 是否开启RedissonLock
     * 1:开启
     * 0:不开启
     *
     * @return
     */
    boolean isOpenRedissonLock() default false;

    /**
     * 是否开启手动控制事务提交
     * 1:开启
     * 0:不开启
     *
     * @return
     */
    boolean isOpenManualCtrlTrans() default false;

    /**
     * 分布式锁key格式：
     * keyFormat a:b:%s
     *
     * @return
     */
    String keyFormat() default "";

    /**
     * 锁定时间
     * 默认 3s
     *
     * @return
     */
    long lockTime() default 3l;

    /**
     * 锁定时间单位
     * TimeUnit.MILLISECONDS 毫秒
     * TimeUnit.SECONDS 秒
     * TimeUnit.MINUTES 分
     * TimeUnit.HOURS 小时
     * TimeUnit.DAYS 天
     *
     * @return
     */
    TimeUnit lockTimeUnit() default TimeUnit.SECONDS;

    /**
     * 是否开启限流
     *
     * @return
     */
    boolean isOpenLimit() default false;

    /**
     * 限流redis失败次数统计key
     * public方法第一个string参数就是%s
     *
     * @return
     */
    String limitRedisKeyPrefix() default "limit:redis:%s";

    /**
     * 限流redisKey统计的key的过期时间
     * 默认10分钟后过期
     *
     * @return
     */
    long limitRedisKeyExpireTime() default 10l;

    /**
     * 锁过期单位
     * TimeUnit.MILLISECONDS 毫秒
     * TimeUnit.SECONDS 秒
     * TimeUnit.MINUTES 分
     * TimeUnit.HOURS 小时
     * TimeUnit.DAYS 天
     *
     * @return
     */
    TimeUnit limitRedisKeyTimeUnit() default TimeUnit.MINUTES;

    /**
     * 默认限流策略：
     * 异常计数器限流：可以根据异常名称和异常内容来计数限制
     * 根据异常次数，当异常次数达到多少次后，限制访问(异常类型为RuntimeException类型) (实现)
     * RedisTemplate的配置文件中需要有这个类型的bean
     *
     * @return
     * @Bean RedisTemplate<String, Long> redisTemplateLimit(RedisConnectionFactory factory) {
     * final RedisTemplate<String, Long> template = new RedisTemplate<>();
     * template.setConnectionFactory(factory);
     * template.setKeySerializer(new StringRedisSerializer());
     * template.setHashValueSerializer(new GenericToStringSerializer<>(Long.class));
     * template.setValueSerializer(new GenericToStringSerializer<>(Long.class));
     * return template;
     * }
     * 滑动窗口限流 (未实现)
     * 令牌桶 (未实现)
     * ip限流 (未实现)
     * Redisson方式限流 (未实现)
     * <p>
     * limitTye() 异常计数器限流 可以写Exception的子类
     * 这个和下面的expContent()互斥,二选一配置即可
     */
    String limitTye() default "";

    /**
     * 异常信息内容匹配统计
     *
     * @return
     */
    String expContent() default "";

    /**
     * 异常统计次数上线默认为10次
     *
     * @return
     */
    int limitMaxErrorCount() default 10;

}
