package com.leaf.example.demo.annotation;

import com.leaf.common.UnresolvedAddress;
import com.leaf.rpc.DefaultProxyFactory;
import com.leaf.rpc.consumer.Consumer;
import com.leaf.rpc.consumer.DefaultConsumer;

import java.util.List;

public class ConsumerExample {

    private static UserService userService;

    static {
        Consumer consumer = new DefaultConsumer("consumer");
        UnresolvedAddress address = new UnresolvedAddress("127.0.0.1", 9180);
        consumer.connect(address);
        consumer.connect(address);

        userService = DefaultProxyFactory.factory(UserService.class)
                .consumer(consumer)
                .providers(address)
                .timeMillis(3000L)
                .newProxy();
    }

    public static void main(String[] args) {
        new ConsumerExample().invoke();
    }

    public void invoke() {
        List<User> allUser = userService.getAllUser();
        System.out.println(allUser);
    }
}
