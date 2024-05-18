## biz-ratelimiter-redissonlock-manualctrltrans-spring-boot-start启动器<br>
1. 项目中引入依赖如下：下面是一个新项目demo的完整pom需要引入的依赖，根据自己项目依赖删减<br>
如果项目中引入如下依赖有缺少依赖需要自行补全所需依赖或者是有依赖冲突需要解决依赖冲突，项目才可以正常启动运行起来<br>
```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>TestLimiter</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <java.version>1.8</java.version>
        <spring-cloud.version>Hoxton.SR9</spring-cloud.version>
        <spring-cloud-alibaba.version>2.2.2.RELEASE</spring-cloud-alibaba.version>
        <commons-collections4.vsersion>4.1</commons-collections4.vsersion>
        <spring-boot.version>2.3.12.RELEASE</spring-boot.version>
        <spring-cloud.version>Hoxton.SR9</spring-cloud.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>${spring-cloud-alibaba.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <!--必须有-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <version>${spring-boot.version}</version>
        </dependency>
        <!--必须有-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
            <version>${spring-boot.version}</version>
        </dependency>
        <!--必须有-->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>
        <!--必须有-->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>1.2.75</version>
            <scope>compile</scope>
        </dependency>
        <!--必须有-->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
        </dependency>
        <!--必须有-->
        <dependency>
            <groupId>com.github.taptap</groupId>
            <artifactId>ratelimiter-spring-boot-starter</artifactId>
            <version>1.3</version>
        </dependency>
        <!--starter依赖必须有-->
        <dependency>
            <groupId>com.zlf</groupId>
            <artifactId>biz-ratelimiter-redissonlock-manualctrltrans-spring-boot-start</artifactId>
            <version>1.0</version>  
        </dependency>
        <!--必须有-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-autoconfigure</artifactId>
            <version>2.3.12.RELEASE</version>
        </dependency>
        <!--必须有-->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>
        <!--必须有-->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-tx</artifactId>
            <version>5.2.15.RELEASE</version>
        </dependency>
        <!--必须有-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
            <version>2.3.9.RELEASE</version>
        </dependency>
        <!--必须有-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
            <version>2.3.12.RELEASE</version>
        </dependency>
        <!--必须有(连接池可以选用其它的连接池依赖)-->
        <dependency>
            <groupId>com.zaxxer</groupId>
            <artifactId>HikariCP</artifactId>
            <version>3.4.0</version>
        </dependency>
        <!--必须有-->
        <dependency>
            <groupId>com.alibaba.nacos</groupId>
            <artifactId>nacos-client</artifactId>
            <version>2.1.2</version>
        </dependency>
        <!--必须有-->
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-pool2</artifactId>
            <version>2.6.0</version>
        </dependency>
        <!--必须有-->
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
            <version>3.10</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>2.22.1</version>
                <configuration>
                    <skipTests>true</skipTests>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
```        
2. nacos配置如下：<br>
```
# 限流ratelimiter配置
spring:
  application:
    name: xxxxx-server
  ratelimiter:
    enabled: true
    redis-address: redis://ip:port
    redis-password: xxxxxx
    response-body: "您请求的太快了,请慢点,不然会有点受不了哦!"
    status-code: 500
  # 数据源需要配置,如果是多数据源将这里改成多数据源配置
  datasource:
    url: jdbc:mysql://xxxx:3306/xxxxxx?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: xxxxx
    driver-class-name: com.mysql.cj.jdbc.Driver
  #redis配置,redis连接池使用默认的lettuce连接池
  redis:
    host: 10.0.40.13
    port: 6389
    password: 12345678
    database: 6
    lettuce:
      pool:
        # 最大空闲连接数
        max-idle: 20
        # 可以分配的最大连接数
        max-active: 50
        # 阻塞的最大时间
        max-wait: 10000
        # 最小空闲连接数
        min-idle: 10
# redison配置  
redisson:
  lock:
    enabled: true
    config:
      address: redis://ip:port
      password: xxxxxx
# 限流业务接口配置
# interfaceName名称可以配置一个相同的，methodName配置不同的方法名称
# 如果interfaceName接口有多个方法需要限流操作就可以配置下面下标0,1的配置
# 也可以配置不同的interfaceName对应不同的methodName的配置
# 相同的methodName对应不同的methodName的配置
# 不要多次配置相同的interfaceName对应相同的methodName的配置
limit:
  lps:
    - interfaceName: xxxxrService1 # 接口类名称首字母小写
      methodName: testLimit1       # 实现类接口名称
      tokenBucketRate: 5           # 可以不配有默认值
      bucketCapacity: 1000         # 可以不配有默认值
      requestedTokens: 1           # 可以不配有默认值
      enabled: true                # 可以不配有默认值(默认开启)
    - interfaceName: xxxxrService1 
      methodName: testLimit2       
      tokenBucketRate: 5         
      bucketCapacity: 1000       
      requestedTokens: 1         
      enabled: true  
    - interfaceName: xxxxrService2
      methodName: testLimit3      
      tokenBucketRate: 5         
      bucketCapacity: 1000       
      requestedTokens: 1         
      enabled: true     
```
3. 启动类上加入如下注解：<br>
   @@EnableZlfBizRateLimiter<br>
4. 功能说明<br>
 4.1 可以单独使用redissonLock分布式锁(@BizIdempotentManualCtrlTransLimiterAnno注解)<br>
 4.2 可以单独使用手动控制事务提交，异常事务回滚(@BizIdempotentManualCtrlTransLimiterAnno注解)<br>
 4.3 可以组合使用4.1和4.2<br>
 4.4 可以单独使用ratelimiter功能(注解方式-ratelimiter开源组件自带的注解)<br>
   ratelimiter开源限流组件github项目地址：https://github.com/TapTap/ratelimiter-spring-boot-starter<br>
 4.5 可以单独使用ratelimiter的令牌桶限流 + 滑动窗口限流<br>
 4.6 可以组合使用4.1、4.2、4.4<br>
 4.7 可以组合使用4.1、4.2、4.5<br>
5. 项目中使用<br>
```
# service层定义一个接口
public interface xxxRateLimiterService {

   RestResponse testLimit(String key, String params);

}
# service层定义一个接口实现类
@Slf4j
@Service
public class xxxBizRateLimiterServiceImpl implements xxxBizRateLimiterService {

    #分布式锁和手动控制事务注解(aop + 自定义注解实现),根据需要加该注解
    #方法格式必须为public修饰,方法第一参数必须为String类型,该参数约定是作为分布式锁的key(传入啥格式就是啥格式)
    #方法中不用使用try/catch否则事务不会回滚,如果使用了try/catch-->在catch中需要把异常抛出(在catch里面throw new RuntimeException(e.getMessage());将异常抛出)到外层给aop代理try/catch才能回滚事务
    @BizIdempotentManualCtrlTransLimiterAnno(isOpenManualCtrlTrans = true, isOpenRedissonLock = true)
    @Override
    public RestResponse testLimit(String key, String params) {
        log.info("=========BizRateLimiterServiceImpl方法开始key:{},params:{}=============", key, params);
        return RestResponse.success(Boolean.TRUE);
    }
    
}

# 
@RestController
@RequestMapping("limit")
public class xxxRateLimiterController {

    #该biz-ratelimiter-redissonlock-manualctrltrans-spring-boot-start组件提供了一套service接口,直接调用即可
    @Autowired
    private BizRateLimiterService bizRateLimiterService;

    #上面xxxRateLimiterService接口的bean
    @Autowired
    private xxxBizRateLimiterService xxxBizRateLimiterService;

    @GetMapping("/testLimit")
    public RestResponse testLimit() {
        #先判断一个配置对应的接口是否开启4.5的功能
        #接口调用的使用index下标需要对应好(配置和接口的调用的时候是通过index来找到是哪个接口的哪个方法)
        if (bizRateLimiterService.enabled(0)) {
            # 滑动窗口限流key,滑动窗口时间是3s内,该key可以传一个唯一的标识,比如用户的id,订单号orderNo,也就是3s内可以限制根据用户唯一标识只能点一次
            String timeWindowKey = "123456";
            # 这里获取接口的代理对象,采用jdk动态代理将原有的接口进行了增强
            XxxBizRateLimiterService  xxxBizRateLimiterService = (XxxBizRateLimiterService ) bizRateLimiterService.getBizRateLimiterProxy(0, xxxBizRateLimiterService , XxxBizRateLimiterService .class, timeWindowKey);
            # 调用目标方法相当于做了两层代理,外层是jdk代理增强实现令牌桶限流 + 滑动窗口限流
            # 内层是aop的动态代理,自定义了一个注解,增强实现了分布式锁和手动控制事务还有其它功能增强
            return xxxBizRateLimiterService .testLimit("testLimit", "6666");
        }
        # 这里是没有开启令牌桶限流 + 滑动窗口限流限流走的是aop注解切面或者是没有aop注解切面的原对象的方法
        return xxxBizRateLimiterService .testLimit("testLimit", "6666");
    }
}
```
6.总结<br>
本来可以将限流的逻辑融合到注解中，但是注解中的逻辑嵌套又比较多，所以不去改原有注解的逻辑，很容易改出bug的，
所以采用jdk动态代理来做了拓展，让ratelimiter的令牌桶限流 + 滑动窗口限流 和 @BizIdempotentManualCtrlTransLimiterAnno注解完美结合使用
本质上是做了两层代理（外层jdk动态代理 + 内层的aop自定义注解切面拦截代理），
可以根据配置灵活设置参数简化重复代码，不用每个项目都去拷贝重复的代码，只需要引入一个依赖，根据约定的配置在利用springBoot的自动装配能力
就可以消除重复代码，让代码化繁为简，业务代码写的更加帅更健壮，更加优雅更加干净更加整洁更加美观，可拓展性、可复用性和可维护性得到非常大的提升。<br>
7.功能简介<br>
限流ratelimiter与注解实现分布式锁、手动控制事务相结合的starter启动器，可以实现接口总体限流、滑动窗口限流+ 分布式锁(防止多次点击)，手动控制事务多表操作异常可以自动回滚事务，保证多表数据一致性