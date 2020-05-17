# rpc
菜鸟学习Netty, RPC,
参考 [Jupiter](https://github.com/fengjiachun/Jupiter) [rocketmq](https://github.com/apache/rocketmq) 设计

#### 目前差不多完成
+ 服务端和客户端长连接 channel复用
+ 多channel连接，断线重连
+ 基于netty写的广播注册中心
+ 集群容错策略
+ 限流
+ 同步调用，异步调用，单向调用
+ 广播调用
+ 泛化调用：不依赖服务端接口
+ 负载均衡算法：加权轮询、加权随机
+ 注解配置


#### 示例：
##### 具体可参考example模块
##### 集成spring示例

##### 1、接口
```` java
public interface HelloService {

    String sayHello(String name);
}
````
##### 2、服务端配置
```` xml
    <bean id="helloService" class="com.leaf.example.spring.HelloServiceImpl"/>

    <leaf:leafServer id="leafServer" registerType="DEFAULT">
        <leaf:property port="9180" />
        <leaf:property registryServer="127.0.0.1:9876"/>
    </leaf:leafServer>

    <leaf:service id="helloServiceProvider" leafServer="leafServer" interfaceClass="com.leaf.example.spring.HelloService" ref="helloService">
        <leaf:property weight="60"/>
        <!-- default leaf -->
        <leaf:property group="spring-demo"/>
        <!-- default class.getName() -->
        <leaf:property serviceProviderName="[test]com.leaf.example.spring.HelloService"/>
        <!-- default 1.0.0 -->
        <leaf:property version="1.1.0"/>
    </leaf:service>
````
##### 3、客户端配置
```` xml
    <leaf:consumer id="consumer" registerType="DEFAULT">
        <leaf:property registryServer="127.0.0.1:9876"/>
    </leaf:consumer>

    <leaf:reference id="helloService" consumer="consumer" interfaceClass="com.leaf.example.spring.HelloService">
        <!-- default leaf -->
        <leaf:property group="spring-demo"/>
        <!-- default class.getName() -->
        <leaf:property serviceProviderName="[test]com.leaf.example.spring.HelloService"/>
        <!-- default 1.0.0 -->
        <leaf:property version="1.1.0"/>
        <!-- default 3000 -->
        <leaf:property timeout="3000"/>
        <!--ROUND, // 单播 BROADCAST;  // 广播 -->
        <leaf:property dispatchType="ROUND"/>
        <!-- PROTO_STUFF, HESSIAN, KRYO,JAVA -->
        <leaf:property serializerType="PROTO_STUFF" />
        <!--RANDOM // 加权随机, ROUND_ROBIN 加权轮询-->
        <leaf:property loadBalancerType="RANDOM" />
        <!--
            FAIL_FAST,  // 快速失败
            FAIL_OVER,  // 失败重试
            FAIL_SAFE,  // 失败安全
        -->
        <leaf:property strategy="FAIL_FAST" />
        <!-- retries 对FAIL_OVER 有效 -->
        <leaf:property retries="0" />
        <!--SYNC //同步, ASYNC //异步, ONE_WAY//单向 -->
        <leaf:property invokeType="SYNC"/>
    </leaf:reference>
````
##### 4、注册中心启动
```` java
public class RegisterServerExample {

    public static void main(String[] args) {
        NettyServerConfig config = new NettyServerConfig();
        config.setPort(9876);

        RegisterServer registerServer = new DefaultRegisterServer(config);
        registerServer.start();
    }
}
````

#### 5、加载服务端spring配置文件启动
```` java
public class ProviderExample {

    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("classpath:/spring/spring-leafServer.xml");
    }
}
````

#### 6、加载客户端spring配置文件启动及调用实例
```` java
public class ConsumerExample {

    public static void main(String[] args) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:/spring/spring-consumer.xml");
        HelloService service = ctx.getBean(HelloService.class);
        try {
            String sayHello = service.sayHello("   biu biu biu");
            System.out.println(sayHello);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
````
