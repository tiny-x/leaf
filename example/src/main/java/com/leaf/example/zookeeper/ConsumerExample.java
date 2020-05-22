package com.leaf.example.zookeeper;

import com.leaf.example.register.HelloService;
import com.leaf.register.api.RegisterType;
import com.leaf.rpc.DefaultProxyFactory;
import com.leaf.rpc.consumer.LeafClient;
import com.leaf.rpc.consumer.DefaultLeafClient;

public class ConsumerExample {

    public static void main(String[] args) throws InterruptedException {
        LeafClient leafClient = new DefaultLeafClient("consumer", RegisterType.ZOOKEEPER);
        leafClient.connectToRegistryServer("121.43.175.216:2181");

        HelloService helloService = DefaultProxyFactory.factory(HelloService.class)
                .consumer(leafClient)
                .timeMillis(3000L)
                .newProxy();

        Thread.sleep(1000);
        String s = helloService.sayHello(" biu biu biu!!!");
        System.out.println(s);

    }
}
