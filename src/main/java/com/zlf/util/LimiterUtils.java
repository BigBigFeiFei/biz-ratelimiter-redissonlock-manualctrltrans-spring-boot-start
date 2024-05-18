package com.zlf.util;

import cn.hutool.core.collection.CollectionUtil;
import com.taptap.ratelimiter.core.RateLimiterService;
import com.zlf.config.LimitProperties;
import com.zlf.dto.LimiterParams;
import com.zlf.handler.MyLimitInvocationHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class LimiterUtils {
    private static ConcurrentHashMap<String, MyLimitInvocationHandler> limitsInvocationHandlersMap = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, LimiterParams> limitParamsMap = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, Object> targetsMap = new ConcurrentHashMap<>();

    private static LimiterParams createLimiterParams(String interfaceName,
                                                     String methodName,
                                                     Integer tokenBucketRate,
                                                     Integer bucketCapacity,
                                                     Integer requestedTokens,
                                                     String tokenBucketKey,
                                                     String timeWindowKey) {
        String key = interfaceName + "-" + methodName;
        LimiterParams limiterParams = limitParamsMap.get(key);
        if (Objects.nonNull(limiterParams)) {
            limiterParams.setTokenBucketRate(tokenBucketRate);
            limiterParams.setBucketCapacity(bucketCapacity);
            limiterParams.setRequestedTokens(requestedTokens);
            limiterParams.setTokenBucketKey(tokenBucketKey);
            limiterParams.setTimeWindowKey(timeWindowKey);
            return limiterParams;
        }
        LimiterParams limiterParams1 = new LimiterParams();
        limiterParams1.setTokenBucketRate(tokenBucketRate);
        limiterParams1.setBucketCapacity(bucketCapacity);
        limiterParams1.setRequestedTokens(requestedTokens);
        limiterParams1.setTokenBucketKey(tokenBucketKey);
        limiterParams1.setTimeWindowKey(timeWindowKey);
        limitParamsMap.put(key, limiterParams1);
        return limiterParams1;
    }

    private static MyLimitInvocationHandler createMyLimitInvocationHandler(String interfaceName,
                                                                           String methodName,
                                                                           RateLimiterService limiterService,
                                                                           LimiterParams limiterParams,
                                                                           Object target) {
        String key = interfaceName + "-" + methodName;
        MyLimitInvocationHandler myLimitInvocationHandler = limitsInvocationHandlersMap.get(key);
        if (Objects.nonNull(myLimitInvocationHandler)) {
            return myLimitInvocationHandler;
        }
        MyLimitInvocationHandler myLimitInvocationHandler1 = new MyLimitInvocationHandler(limiterService, limiterParams, target);
        limitsInvocationHandlersMap.put(key, myLimitInvocationHandler1);
        return myLimitInvocationHandler1;
    }

    public static Object createTarget(Class targetClazz) {
        String simpleName = targetClazz.getSimpleName();
        Object target = targetsMap.get(simpleName);
        if (Objects.nonNull(target)) {
            return target;
        }
        try {
            Object tg = targetClazz.newInstance();
            targetsMap.put(simpleName, tg);
            return tg;
        } catch (Exception e) {
            log.error("限流创建目标对象异常:{}", e.getMessage());
        }
        return null;
    }

    /**
     * 哪个代理类接口的哪个方法需要使用2种限流组合限流(令牌桶 + 滑动窗口),
     * 可以达到接口总体令牌桶限流和针对接口用户表示进行滑动窗口限流(比如说根据用户id进行滑动窗口限流:只允许3秒内某用户只能操作一次,防止用户3秒内点击多次)
     * 目标类的方法上加上如下注解：开启分布式锁功能和手动控制事务功能
     *
     * @param interfaceName        接口名称(唯一)(必传)
     * @param methodName           方法名称(唯一)(必传)
     * @param tokenBucketRate      令牌通生产速率(有默认值)
     * @param bucketCapacity       令牌桶容量(有默认值)
     * @param requestedTokens      请求的令牌数(有默认值) 非必传
     * @param tokenBucketKey       令牌通限流key一般传方法名称(唯一即可)(必传)
     * @param timeWindowKey        滑动窗口限流key一般传唯一标识：比如用户id等(必传)
     * @param limiterService       限流服务(必传)
     * @param target               需要代理的接口对象(spring注入的bean:比如加了@Service注解的类或其它注解注入的bean,该实例需要实现自定义的业务接口)(必传)
     * @param targetInterfaceClazz 目标对象的接口类的Clazz
     * @param <T>
     * @return
     * @BizIdempotentManualCtrlTransLimiterAnno(isOpenManualCtrlTrans = true, isOpenRedissonLock = true)
     * 方法格式如下：
     * 注意：
     * 1.方法必须使用public修饰,否则无法被aop代理拦截
     * 2.方法的第一个参数必须是String,该参数是开始分布式锁功能的,分布式锁的key,格式自行定义即可
     * 3.开启手动控制事务功能,方法种不能加try/catch,如果加了try/catch需要在catch里把异常往外抛出,否则aop外层拦截try/catch不到异常,事务就不会回滚
     * 格式一：
     * public T xxx方法名称(String key, X x,,,,,) {
     * ,,,,,,,,,
     * return xx;
     * }
     * 格式二：
     * public T xxx方法名称(String key, X x,,,,,) {
     * ,,,,,,,,,
     * try{
     * ,,,,,,,,,
     * }catch(Exception e){
     * throw new RuntimeException(e.getMessage());
     * }finally{
     * ,,,,,,,,,
     * }
     * return xx;
     * }
     */
    private static <T> T createLimiterProxyInstance1(String interfaceName,
                                                     String methodName,
                                                     Integer tokenBucketRate,
                                                     Integer bucketCapacity,
                                                     Integer requestedTokens,
                                                     String tokenBucketKey,
                                                     String timeWindowKey,
                                                     RateLimiterService limiterService,
                                                     Object target,
                                                     Class targetInterfaceClazz) {
        if (StringUtils.isBlank(interfaceName) && StringUtils.isBlank(methodName)) {
            throw new RuntimeException("创建限流代理对象interfaceName,methodName不为空");
        }
        if (Objects.isNull(tokenBucketRate)) {
            //令牌通生产速率
            tokenBucketRate = 10;
        }
        if (Objects.isNull(bucketCapacity)) {
            //令牌桶容量
            bucketCapacity = 100;
        }
        if (Objects.isNull(requestedTokens)) {
            //请求的令牌数
            requestedTokens = 1;
        }
        if (StringUtils.isBlank(tokenBucketKey)) {
            throw new RuntimeException("创建限流代理对象tokenBucketKey不为空");
        }
        if (StringUtils.isBlank(timeWindowKey)) {
            throw new RuntimeException("创建限流代理对象timeWindowKey不为空");
        }
        if (Objects.isNull(limiterService)) {
            throw new RuntimeException("创建限流代理对象limiterService不为空");
        }
        if (Objects.isNull(targetInterfaceClazz)) {
            throw new RuntimeException("创建限流代理类:目标类的接口类型targetInterfaceClazz不为空");
        }
        if (Objects.isNull(target)) {
            throw new RuntimeException("创建限流代理类:目标类的实现类对象targetImpl不为空");
        }
        LimiterParams limiterParams = createLimiterParams(interfaceName, methodName, tokenBucketRate, bucketCapacity, requestedTokens, tokenBucketKey, timeWindowKey);
        MyLimitInvocationHandler myLimitInvocationHandler = createMyLimitInvocationHandler(interfaceName, methodName, limiterService, limiterParams, target);
        T limiterProxyInstance = JdkDynamicProxyUtils.createLimiterProxyInstance(targetInterfaceClazz, myLimitInvocationHandler);
        return limiterProxyInstance;
    }

    public static <T> T createLimiterProxyInstance2(LimitProperties limitProperties,
                                                    String timeWindowKey,
                                                    RateLimiterService limiterService,
                                                    Class targetInterfaceClazz,
                                                    Object target) throws Exception {
        Class<?>[] interfaces = target.getClass().getInterfaces();
        if (CollectionUtil.isEmpty(Arrays.asList(interfaces))) {
            throw new RuntimeException("限流传入的targetClazz创建的对象实例没有实现一个接口!");
        }
        return createLimiterProxyInstance1(
                limitProperties.getInterfaceName(),
                limitProperties.getMethodName(),
                limitProperties.getTokenBucketRate(),
                limitProperties.getBucketCapacity(),
                limitProperties.getRequestedTokens(),
                limitProperties.getInterfaceName() + "-" + limitProperties.getMethodName(),
                timeWindowKey,
                limiterService,
                target,
                targetInterfaceClazz
        );
    }

}
