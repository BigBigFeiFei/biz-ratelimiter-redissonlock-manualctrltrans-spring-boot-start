package com.zlf.config;

import com.alibaba.fastjson.JSON;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.MasterSlaveServersConfig;
import org.redisson.config.SentinelServersConfig;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Objects;

/**
 * 分布式锁自动化配置
 *
 * @author zlf
 */
@Slf4j
@Configuration
@ConditionalOnClass(RedissonClient.class)
@EnableConfigurationProperties(RedissonLockProperties.class)
@ConditionalOnProperty(value = "redisson.lock.enabled", havingValue = "true")
public class RedissonLockAutoConfiguration implements ImportBeanDefinitionRegistrar, EnvironmentAware, BeanFactoryAware {

    private BeanFactory beanFactory;

    private RedissonLockProperties redissonLockProperties;

    public final static String REDISSON_LOCK_BEAN_NAME = "RedissonLockBeanName";


    public Config singleConfig(RedissonLockProperties properties) {
        Config config = new Config();
        SingleServerConfig serversConfig = config.useSingleServer();
        serversConfig.setAddress(properties.getAddress());
        String password = properties.getPassword();
        if (StringUtil.isNotBlank(password)) {
            serversConfig.setPassword(password);
        }
        serversConfig.setDatabase(properties.getDatabase());
        serversConfig.setConnectionPoolSize(properties.getPoolSize());
        serversConfig.setConnectionMinimumIdleSize(properties.getIdleSize());
        serversConfig.setIdleConnectionTimeout(properties.getConnectionTimeout());
        serversConfig.setConnectTimeout(properties.getConnectionTimeout());
        serversConfig.setTimeout(properties.getTimeout());
        return config;
    }

    public Config masterSlaveConfig(RedissonLockProperties properties) {
        Config config = new Config();
        MasterSlaveServersConfig serversConfig = config.useMasterSlaveServers();
        serversConfig.setMasterAddress(properties.getMasterAddress());
        serversConfig.addSlaveAddress(properties.getSlaveAddress());
        String password = properties.getPassword();
        if (StringUtil.isNotBlank(password)) {
            serversConfig.setPassword(password);
        }
        serversConfig.setDatabase(properties.getDatabase());
        serversConfig.setMasterConnectionPoolSize(properties.getPoolSize());
        serversConfig.setMasterConnectionMinimumIdleSize(properties.getIdleSize());
        serversConfig.setSlaveConnectionPoolSize(properties.getPoolSize());
        serversConfig.setSlaveConnectionMinimumIdleSize(properties.getIdleSize());
        serversConfig.setIdleConnectionTimeout(properties.getConnectionTimeout());
        serversConfig.setConnectTimeout(properties.getConnectionTimeout());
        serversConfig.setTimeout(properties.getTimeout());
        return config;
    }

    public Config sentinelConfig(RedissonLockProperties properties) {
        Config config = new Config();
        SentinelServersConfig serversConfig = config.useSentinelServers();
        serversConfig.setMasterName(properties.getMasterName());
        serversConfig.addSentinelAddress(properties.getSentinelAddress());
        String password = properties.getPassword();
        if (StringUtil.isNotBlank(password)) {
            serversConfig.setPassword(password);
        }
        serversConfig.setDatabase(properties.getDatabase());
        serversConfig.setMasterConnectionPoolSize(properties.getPoolSize());
        serversConfig.setMasterConnectionMinimumIdleSize(properties.getIdleSize());
        serversConfig.setSlaveConnectionPoolSize(properties.getPoolSize());
        serversConfig.setSlaveConnectionMinimumIdleSize(properties.getIdleSize());
        serversConfig.setIdleConnectionTimeout(properties.getConnectionTimeout());
        serversConfig.setConnectTimeout(properties.getConnectionTimeout());
        serversConfig.setTimeout(properties.getTimeout());
        return config;
    }

    public Config clusterConfig(RedissonLockProperties properties) {
        Config config = new Config();
        ClusterServersConfig serversConfig = config.useClusterServers();
        serversConfig.addNodeAddress(properties.getNodeAddress());
        String password = properties.getPassword();
        if (StringUtil.isNotBlank(password)) {
            serversConfig.setPassword(password);
        }
        serversConfig.setMasterConnectionPoolSize(properties.getPoolSize());
        serversConfig.setMasterConnectionMinimumIdleSize(properties.getIdleSize());
        serversConfig.setSlaveConnectionPoolSize(properties.getPoolSize());
        serversConfig.setSlaveConnectionMinimumIdleSize(properties.getIdleSize());
        serversConfig.setIdleConnectionTimeout(properties.getConnectionTimeout());
        serversConfig.setConnectTimeout(properties.getConnectionTimeout());
        serversConfig.setTimeout(properties.getTimeout());
        return config;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        RedissonClient redissonClient = null;
        int mode = redissonLockProperties.getMode();
        log.info("RedissonLockAutoConfiguration.redissonLockProperties:{}", JSON.toJSONString(redissonLockProperties));
        log.info("RedissonLockAutoConfiguration.redissonLockProperties.model:{}", mode);
        Config config = null;
        switch (mode) {
            case 1:
                config = singleConfig(redissonLockProperties);
                redissonClient = Redisson.create(config);
                break;
            case 2:
                config = masterSlaveConfig(redissonLockProperties);
                redissonClient = Redisson.create(config);
                break;
            case 3:
                config = sentinelConfig(redissonLockProperties);
                redissonClient = Redisson.create(config);
                break;
            case 4:
                config = clusterConfig(redissonLockProperties);
                redissonClient = Redisson.create(config);
                break;
        }
        if (Objects.isNull(redissonClient)) {
            log.info("RedissonLockAutoConfiguration注册RedissonLockBeanName未获取到redissonClient实例,不注入bean");
            return;
        }
        ((ConfigurableBeanFactory) this.beanFactory).registerSingleton(REDISSON_LOCK_BEAN_NAME, redissonClient);
        log.info("RedissonLockAutoConfiguration注册RedissonLockBeanName完成,beanName:{}", REDISSON_LOCK_BEAN_NAME);
    }


    @Override
    public void setEnvironment(Environment environment) {
        // 通过Binder将environment中的值转成对象
        redissonLockProperties = Binder.get(environment).bind(getPropertiesPrefix(RedissonLockProperties.class), RedissonLockProperties.class).get();
    }

    private String getPropertiesPrefix(Class<?> tClass) {
        return Objects.requireNonNull(AnnotationUtils.getAnnotation(tClass, ConfigurationProperties.class)).prefix();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

}
