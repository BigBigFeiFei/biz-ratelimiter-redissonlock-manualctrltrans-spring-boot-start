package com.zlf.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * 虽然加了@RefreshScope但是RedissonClient客户端已经创建,
 * 修改了RedissonLockProperties文件需要重启项目重新创建RedissonClient客户端的bean
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = "redisson.lock.config")
public class RedissonLockProperties {

    private String address;

    private String password;

    /**
     * 1.single
     * 2.master
     * 3.sentinel
     * 4.cluster
     */
    private int mode = 1;

    /**
     * 在master模式下需配置这个
     */
    private String masterAddress;

    /**
     * 在master模式下需配置这个
     */
    private String[] slaveAddress;

    /**
     * 在sentinel模式下需配置这个
     */
    private String masterName;

    /**
     * 在sentinel模式下需配置这个
     */
    private String[] sentinelAddress;

    /**
     * 在cluster模式下需配置这个
     */
    private String[] nodeAddress;

    private int database = 5;

    private int poolSize = 64;

    private int idleSize = 24;

    private int connectionTimeout = 10000;

    private int timeout = 3000;

}
