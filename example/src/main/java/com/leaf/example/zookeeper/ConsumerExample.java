package com.leaf.example.zookeeper;

import com.leaf.example.register.HelloService;
import com.leaf.register.api.RegisterType;
import com.leaf.rpc.ProxyFactory;
import com.leaf.rpc.consumer.Consumer;
import com.leaf.rpc.consumer.DefaultConsumer;

public class ConsumerExample {

    public static void main(String[] args) throws InterruptedException {
        Consumer consumer = new DefaultConsumer("consumer", RegisterType.ZOOKEEPER);
        consumer.connectToRegistryServer("127.0.0.1:2181");

        HelloService helloService = ProxyFactory.factory(HelloService.class)
                .consumer(consumer)
                .timeMillis(3000L)
                .newProxy();

        Thread.sleep(1000);
        String s = helloService.sayHello(" biu biu biu!!!");
        System.out.println(s);

    }
}
