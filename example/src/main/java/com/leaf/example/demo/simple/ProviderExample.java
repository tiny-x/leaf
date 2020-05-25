package com.leaf.example.demo.simple;

import com.leaf.example.demo.HelloService;
import com.leaf.example.demo.HelloServiceImpl;
import com.leaf.rpc.provider.DefaultLeafServer;
import com.leaf.rpc.provider.LeafServer;

public class ProviderExample {

    public static void main(String[] args) {
        LeafServer leafServer = new DefaultLeafServer(9180);
        leafServer.start();

        leafServer.serviceRegistry()
                .provider(new HelloServiceImpl())
                .interfaceClass(HelloService.class)
                .register();
    }
}
