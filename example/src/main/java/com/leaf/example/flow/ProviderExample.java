package com.leaf.example.flow;

import com.leaf.rpc.local.ServiceWrapper;
import com.leaf.remoting.netty.NettyServerConfig;
import com.leaf.rpc.controller.FlowController;
import com.leaf.rpc.provider.DefaultProvider;
import com.leaf.rpc.provider.Provider;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class ProviderExample {

    public static void main(String[] args) {
        NettyServerConfig config = new NettyServerConfig();
        Provider provider = new DefaultProvider(config);
        provider.start();
        provider.registerGlobalFlowController(new FlowController() {

            private AtomicInteger atomicInteger = new AtomicInteger(0);

            @Override
            public void flowController() throws RejectedExecutionException {
                if (atomicInteger.incrementAndGet() > 100) {
                    throw new RejectedExecutionException("flowController count:" + atomicInteger.get());
                }
            }
        });

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
