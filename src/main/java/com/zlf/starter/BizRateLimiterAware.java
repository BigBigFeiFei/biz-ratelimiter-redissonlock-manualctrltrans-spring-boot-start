package com.zlf.starter;

import com.alibaba.fastjson.JSON;
import com.taptap.ratelimiter.core.RateLimiterService;
import com.zlf.config.LimitConfig;
import com.zlf.config.LimitProperties;
import com.zlf.constants.BizRateLimiterConstants;
import com.zlf.dto.LimiterParams;
import com.zlf.handler.MyLimitInvocationHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;


/**
 * @author zlf
 */
@Component
@Slf4j
public class BizRateLimiterAware implements ApplicationContextAware, EnvironmentAware, BeanFactoryAware {

    @Autowired
    private RateLimiterService limiterService;

    private BeanFactory beanFactory;

    private LimitConfig limitConfig;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        List<LimitProperties> lps = limitConfig.getLps();
        log.info("===========BizRateLimiterAware.lps:{}=============", JSON.toJSONString(lps));
        for (int i = 0; i < lps.size(); i++) {
            if (StringUtils.isBlank(lps.get(i).getInterfaceName())) {
                throw new RuntimeException("BizRateLimiterAware.第{" + i + "}个LimitProperties的interfaceName不为空!");
            }
            if (StringUtils.isBlank(lps.get(i).getMethodName())) {
                throw new RuntimeException("BizRateLimiterAware.第{" + i + "}个LimitProperties的methodName不为空!");
            }
            LimiterParams limiterParams = new LimiterParams();
            limiterParams.setTokenBucketKey(lps.get(i).getInterfaceName() + "-" + lps.get(i).getMethodName());
            //暂时给空值
            limiterParams.setTimeWindowKey("待传参数");
            ((ConfigurableBeanFactory) this.beanFactory).registerSingleton(BizRateLimiterConstants.LimiterParams_BEAN_NAME_PREFIX + i, limiterParams);
            log.info("BizRateLimiterAware注入beanName:{}成功", BizRateLimiterConstants.LimiterParams_BEAN_NAME_PREFIX + i);
            MyLimitInvocationHandler myLimitInvocationHandler = new MyLimitInvocationHandler(limiterService, limiterParams, null);
            ((ConfigurableBeanFactory) this.beanFactory).registerSingleton(BizRateLimiterConstants.MyLimitInvocationHandler_BEAN_NAME_PREFIX + i, myLimitInvocationHandler);
            log.info("BizRateLimiterAware注入beanName:{}成功", BizRateLimiterConstants.MyLimitInvocationHandler_BEAN_NAME_PREFIX + i);
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        // 通过Binder将environment中的值转成对象
        limitConfig = Binder.get(environment).bind(getPropertiesPrefix(LimitConfig.class), LimitConfig.class).get();
    }

    private String getPropertiesPrefix(Class<?> tClass) {
        return Objects.requireNonNull(AnnotationUtils.getAnnotation(tClass, ConfigurationProperties.class)).prefix();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }


}
