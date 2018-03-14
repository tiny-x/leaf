package com.leaf.example.demo.basic;

import com.leaf.rpc.local.ServiceWrapper;
import com.leaf.example.demo.HelloService;
import com.leaf.example.demo.HelloServiceImpl;
import com.leaf.remoting.netty.NettyServerConfig;
import com.leaf.rpc.controller.RateLimitFlowController;
import com.leaf.rpc.provider.DefaultProvider;
import com.leaf.rpc.provider.Provider;

public class ProviderExample {

    public static void main(String[] args) {
        NettyServerConfig config = new NettyServerConfig();
        config.setPort(9180);
        Provider provider = new DefaultProvider(config);
        provider.start();
        provider.registerGlobalFlowController(new RateLimitFlowController(100000));
        HelloService helloService = new HelloServiceImpl();

        // 注册到本地容器 未发布到注册中心
        ServiceWrapper serviceWrapper = provider.serviceRegistry()
                .provider(helloService)
                .interfaceClass(HelloService.class)
                .providerName("org.rpc.example.demo.HelloService")
                .group("test")
                .version("1.0.0")
                .register();

    }
}
