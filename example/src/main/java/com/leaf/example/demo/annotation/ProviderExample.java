package com.leaf.example.demo.annotation;

import com.leaf.rpc.local.ServiceWrapper;
import com.leaf.rpc.provider.DefaultProvider;
import com.leaf.rpc.provider.Provider;

public class ProviderExample {

    public static void main(String[] args) {

        Provider provider = new DefaultProvider(9180);
        provider.start();

        UserService userService = new UserServiceImpl();

        // 注册到本地容器 未发布到注册中心
        ServiceWrapper serviceWrapper = provider.serviceRegistry()
                .provider(userService)
                .register();

    }
}
