package com.leaf.example.register;

import com.leaf.rpc.DefaultProxyFactory;
import com.leaf.rpc.consumer.DefaultLeafClient;
import com.leaf.rpc.consumer.LeafClient;

public class ConsumerExample {

    public static void main(String[] args) {
        LeafClient leafClient = new DefaultLeafClient("consumer");
        leafClient.connectToRegistryServer("127.0.0.1:9876");

        HelloService helloService = DefaultProxyFactory.factory(HelloService.class)
                .consumer(leafClient)
                .timeMillis(3000L)
                .newProxy();

        String s = helloService.sayHello(" biu biu biu!!!");
        System.out.println(s);

    }
}
