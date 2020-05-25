package com.leaf.example.demo.simple;

import com.leaf.common.UnresolvedAddress;
import com.leaf.example.demo.HelloService;
import com.leaf.example.demo.User;
import com.leaf.rpc.DefaultProxyFactory;
import com.leaf.rpc.consumer.DefaultLeafClient;
import com.leaf.rpc.consumer.LeafClient;

public class ConsumerExample {

    public static void main(String[] args) {
        LeafClient leafClient = new DefaultLeafClient("consumer");

       HelloService helloService = DefaultProxyFactory.factory(HelloService.class)
                .consumer(leafClient)
                .providers(new UnresolvedAddress("127.0.0.1", 9180))
                .newProxy();
        System.out.println(helloService.sayHello("i'm king", "119"));
        System.out.println(helloService.sayHello(new User("达维安爵士", "108")));
    }
}
