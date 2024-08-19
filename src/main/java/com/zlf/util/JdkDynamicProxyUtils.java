package com.zlf.util;


import com.zlf.handler.MyLimitInvocationHandler;

import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class JdkDynamicProxyUtils {

    private static ConcurrentHashMap<String, Object> limiterProxyInstanceMap = new ConcurrentHashMap<>();

    private static String KEY_SUFFIX = "proxy";

    /**
     * 创建限流代理类实例
     *
     * @return
     */
    public static <T> T createLimiterProxyInstance(Class targetInterfaceClazz, MyLimitInvocationHandler myLimitInvocationHandler) {
        String key = targetInterfaceClazz.getSimpleName() + "-" + KEY_SUFFIX;
        Object obj = limiterProxyInstanceMap.get(key);
        if (Objects.nonNull(obj)) {
            return (T) obj;
        }
        // 创建并返回代理类实例
        T t = (T) Proxy.newProxyInstance(
                targetInterfaceClazz.getClassLoader(),
                new Class[]{targetInterfaceClazz},
                myLimitInvocationHandler
        );
        limiterProxyInstanceMap.put(key, t);
        return t;
    }

}
