package com.leaf.example.register;

import com.leaf.rpc.ProxyFactory;
import com.leaf.rpc.consumer.Consumer;
import com.leaf.rpc.consumer.DefaultConsumer;

public class ConsumerExample {

    public static void main(String[] args) {
        Consumer consumer = new DefaultConsumer("consumer");
        consumer.connectToRegistryServer("127.0.0.1:9876");

        HelloService helloService = ProxyFactory.factory(HelloService.class)
                .consumer(consumer)
                .timeMillis(3000L)
                .newProxy();

        String s = helloService.sayHello(" biu biu biu!!!");
        System.out.println(s);

    }
}
