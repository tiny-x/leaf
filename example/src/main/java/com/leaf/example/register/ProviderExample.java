package com.leaf.example.register;

import com.leaf.common.model.ServiceWrapper;
import com.leaf.remoting.netty.NettyServerConfig;
import com.leaf.rpc.provider.DefaultProvider;
import com.leaf.rpc.provider.Provider;

public class ProviderExample {

    public static void main(String[] args) {
        NettyServerConfig config = new NettyServerConfig();
        Provider provider = new DefaultProvider(config);
        provider.start();
        provider.connectToRegistryServer("127.0.0.1:9876");

        HelloService helloService = new HelloServiceImpl();

        // 注册到本地容器 未发布到注册中心
        ServiceWrapper serviceWrapper = provider.serviceRegistry()
                .provider(helloService)
                .interfaceClass(HelloService.class)
                .providerName("org.rpc.example.demo.HelloService")
                .group("test")
                .version("1.0.0")
                .weight(50)
                .register();

        // 发布到注册中心
        provider.publishService(serviceWrapper);

    }
}
