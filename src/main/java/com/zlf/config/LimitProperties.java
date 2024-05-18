package com.zlf.config;

import lombok.Data;

@Data
public class LimitProperties {

    /**
     * 接口名称(类/接口名)--简名(必传)
     */
    private String interfaceName;

    /**
     * 方法名称-简名(必传)
     */
    private String methodName;

    /**
     * 令牌通生产速率(选传)
     */
    private Integer tokenBucketRate = 10;

    /**
     * 令牌桶容量(选传)
     */
    private Integer bucketCapacity = 100;

    /**
     * 请求的令牌数(选传)
     */
    private Integer requestedTokens = 1;

    /**
     * 是否开启限流(选传:默认开启)
     */
    private Boolean enabled = Boolean.TRUE;

}
