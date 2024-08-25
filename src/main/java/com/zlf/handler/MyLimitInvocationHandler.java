package com.zlf.handler;

import com.taptap.ratelimiter.core.RateLimiterService;
import com.taptap.ratelimiter.model.Mode;
import com.taptap.ratelimiter.model.Result;
import com.taptap.ratelimiter.model.Rule;
import com.zlf.dto.LimiterParams;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
@Data
public class MyLimitInvocationHandler implements InvocationHandler {

    private RateLimiterService limiterService;

    private LimiterParams limiterParams;

    private Object target;

    public MyLimitInvocationHandler(RateLimiterService limiterService, LimiterParams limiterParams, Object target) {
        this.limiterService = limiterService;
        this.limiterParams = limiterParams;
        this.target = target;
    }


    /**
     * 代理执行方法：利用代理类实例执行目标类的方法都会进入到invoke方法中执行
     *
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("==================MyLimitInvocationHandler处理逻辑开始===========================");
        // 限流策略,设置为令牌桶
        Rule rule1 = new Rule(Mode.TOKEN_BUCKET);
        if (StringUtils.isBlank(limiterParams.getTokenBucketKey())) {
            throw new RuntimeException("限流参数tokenBucketKey不为空!");
        }
        //限流的 key
        rule1.setKey(limiterParams.getTokenBucketKey());
        //每秒产生的令牌数
        rule1.setRate(limiterParams.getTokenBucketRate());
        //令牌桶容量
        rule1.setBucketCapacity(limiterParams.getBucketCapacity());
        //请求的令牌数
        rule1.setRequestedTokens(limiterParams.getRequestedTokens());
        Result result1 = limiterService.isAllowed(rule1);
        //如果允许访问
        if (result1.isAllow()) {
            // 限流策略,设置为时间窗口
            Rule rule2 = new Rule(Mode.TIME_WINDOW);
            if (StringUtils.isBlank(limiterParams.getTimeWindowKey())) {
                throw new RuntimeException("限流参数timeWindowKey不为空!");
            }
            //限流的 key
            rule2.setKey(limiterParams.getTimeWindowKey());
            //限流的速率
            rule2.setRate(limiterParams.getRate());
            //时间窗口大小，单位为秒
            rule2.setRateInterval(limiterParams.getRateInterval());
            Result result2 = limiterService.isAllowed(rule2);
            //如果允许访问
            if (result2.isAllow()) {
                // 利用目标类实例执行原有的逻辑
                try {
                    Object result = method.invoke(target, args);
                    return result;
                } catch (InvocationTargetException e) {
                    Throwable t = e.getTargetException();
                    log.error("MyLimitInvocationHandler接口异常:{}", t.getMessage());
                    throw new RuntimeException(t.getMessage());
                }
            } else {
                //触发限流
                throw new RuntimeException("您手速太快了,请慢点操作哈!");
            }
        } else {
            //触发限流
            throw new RuntimeException("当前访问人数较多,请您稍后重试!");
        }
    }

}
