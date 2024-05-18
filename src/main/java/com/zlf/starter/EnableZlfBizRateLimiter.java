package com.zlf.starter;

import com.zlf.annotation.BizIdempotentManualCtrlTransAspect;
import com.zlf.service.BizRateLimiterService;
import com.zlf.util.BizRateLimiterSpringUtils;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用需要在启动类上加入@EnableZlfBizRateLimiter注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@EnableTransactionManagement
@Import({BizRateLimiterAware.class, BizRateLimiterService.class, BizRateLimiterSpringUtils.class})
public @interface EnableZlfBizRateLimiter {

}
