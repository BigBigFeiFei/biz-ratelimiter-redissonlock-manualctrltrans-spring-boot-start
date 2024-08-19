package com.zlf.service;

import cn.hutool.core.collection.CollectionUtil;
import com.zlf.config.LimitConfig;
import com.zlf.config.LimitProperties;
import com.zlf.constants.BizRateLimiterConstants;
import com.zlf.dto.LimiterParams;
import com.zlf.handler.MyLimitInvocationHandler;
import com.zlf.util.BizRateLimiterSpringUtils;
import com.zlf.util.JdkDynamicProxyUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Objects;

@Slf4j
@Service
public class BizRateLimiterService {

    @Autowired
    private LimitConfig limitConfig;

    public Boolean enabled(Integer index) {
        LimitProperties limitProperties = this.getLimitProperties(index);
        return Objects.nonNull(limitProperties) ? limitProperties.getEnabled() : Boolean.FALSE;
    }

    public Object getBizRateLimiterProxy(Integer index, Object target, Class targetInterfaceClazz, String timeWindowKey) {
        LimitProperties limitProperties = this.getLimitProperties(index);
        if (index < 0 || index >= limitConfig.getLps().size()) {
            throw new RuntimeException("BizRateLimiterService.index有误");
        }
        if (Objects.isNull(limitProperties)) {
            throw new RuntimeException("BizRateLimiterService.index未找到对应的limitProperties");
        }
        if (Objects.isNull(target)) {
            throw new RuntimeException("BizRateLimiterService.代理对象target实例不为空");
        }
        if (Objects.isNull(targetInterfaceClazz)) {
            throw new RuntimeException("BizRateLimiterService.代理对象targetInterfaceClazz不为空");
        }
        if (StringUtils.isBlank(timeWindowKey)) {
            throw new RuntimeException("BizRateLimiterService.timeWindowKey不为空");
        }
        Class<?>[] interfaces = target.getClass().getInterfaces();
        if (CollectionUtil.isEmpty(Arrays.asList(interfaces))) {
            throw new RuntimeException("BizRateLimiterService传入的target没有实现一个接口!");
        }
        LimiterParams limiterParams = (LimiterParams) BizRateLimiterSpringUtils.getBean(BizRateLimiterConstants.LimiterParams_BEAN_NAME_PREFIX + index);
        limiterParams.setTokenBucketRate(limitProperties.getTokenBucketRate());
        limiterParams.setBucketCapacity(limitProperties.getBucketCapacity());
        limiterParams.setRequestedTokens(limitProperties.getRequestedTokens());
        limiterParams.setTokenBucketKey(limitProperties.getInterfaceName() + "-" + limitProperties.getMethodName());
        limiterParams.setTimeWindowKey(timeWindowKey);
        limiterParams.setRate(limitProperties.getRate());
        limiterParams.setRateInterval(limitProperties.getRateInterval());

        MyLimitInvocationHandler myLimitInvocationHandler = (MyLimitInvocationHandler) BizRateLimiterSpringUtils.getBean(BizRateLimiterConstants.MyLimitInvocationHandler_BEAN_NAME_PREFIX + index);
        myLimitInvocationHandler.setLimiterParams(limiterParams);
        myLimitInvocationHandler.setTarget(target);

        Object bizRateLimiterProxy = JdkDynamicProxyUtils.createLimiterProxyInstance(targetInterfaceClazz, myLimitInvocationHandler);
        return bizRateLimiterProxy;
    }

    public LimitProperties getLimitProperties(Integer index) {
        return limitConfig.getLps().get(index);
    }

}
