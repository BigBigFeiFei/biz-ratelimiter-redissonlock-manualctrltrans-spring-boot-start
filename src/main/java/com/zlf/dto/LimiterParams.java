package com.zlf.dto;

import lombok.Data;

@Data
public class LimiterParams {

    /**
     * 令牌通生产速率
     */
    private Integer tokenBucketRate = 10;

    /**
     * 令牌桶容量
     */
    private Integer bucketCapacity = 100;

    /**
     * 请求的令牌数
     */
    private Integer requestedTokens = 1;

    /**
     * 令牌通限流key--一般写方法名即可
     */
    private String tokenBucketKey;

    /**
     * 滑动窗口限流key--一般写唯一标识,比如用户id,保证唯一即可
     */
    private String timeWindowKey;

    /**
     * 限流的速率 默认是1
     */
    private Integer rate = 1;

    /**
     * 时间窗口大小，单位为秒  默认是3s
     */
    private Integer rateInterval = 3;


}
