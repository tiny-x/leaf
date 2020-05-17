package com.leaf.example.demo.annotation;

import com.leaf.rpc.local.ServiceWrapper;
import com.leaf.rpc.provider.DefaultLeafServer;
import com.leaf.rpc.provider.LeafServer;

public class ProviderExample {

    public static void main(String[] args) {

        LeafServer leafServer = new DefaultLeafServer(9180);
        leafServer.start();

        UserService userService = new UserServiceImpl();

        // 注册到本地容器 未发布到注册中心
        ServiceWrapper serviceWrapper = leafServer.serviceRegistry()
                .provider(userService)
                .register();

    }
}
