package com.leaf.example.register;

import com.leaf.common.model.ServiceMeta;
import com.leaf.remoting.netty.NettyClientConfig;
import com.leaf.rpc.ProxyFactory;
import com.leaf.rpc.consumer.Consumer;
import com.leaf.rpc.consumer.DefaultConsumer;

public class ConsumerExample {

    public static void main(String[] args) {
        NettyClientConfig config = new NettyClientConfig();
        Consumer consumer = new DefaultConsumer("consumer", config);
        consumer.connectToRegistryServer("127.0.0.1:9876");

        ServiceMeta serviceMeta = new ServiceMeta("test", "org.rpc.example.demo.HelloService", "1.0.0");

        HelloService helloService = ProxyFactory.factory(HelloService.class)
                .consumer(consumer)
                .directory(serviceMeta)
                .timeMillis(300000L)
                .newProxy();

        String s = helloService.sayHello(" biu biu biu!!!");
        System.out.println(s);

    }
}
