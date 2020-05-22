package com.leaf.example.demo.annotation;

import com.leaf.common.UnresolvedAddress;
import com.leaf.rpc.DefaultProxyFactory;
import com.leaf.rpc.consumer.DefaultLeafClient;
import com.leaf.rpc.consumer.LeafClient;

import java.util.List;

public class ConsumerExample {

    private static UserService userService;

    static {
        LeafClient leafClient = new DefaultLeafClient("consumer");
        UnresolvedAddress address = new UnresolvedAddress("127.0.0.1", 9180);
        leafClient.connect(address);
        leafClient.connect(address);

        userService = DefaultProxyFactory.factory(UserService.class)
                .consumer(leafClient)
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
