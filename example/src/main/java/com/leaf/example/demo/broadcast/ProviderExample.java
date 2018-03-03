package com.leaf.example.demo.broadcast;

import com.leaf.common.model.ServiceWrapper;
import com.leaf.example.demo.HelloService;
import com.leaf.example.demo.HelloServiceImpl;
import com.leaf.remoting.netty.NettyServerConfig;
import com.leaf.rpc.controller.RateLimitFlowController;
import com.leaf.rpc.provider.DefaultProvider;
import com.leaf.rpc.provider.Provider;

import java.util.concurrent.CountDownLatch;

public class ProviderExample {

    public static void main(String[] args) throws InterruptedException {

        Provider[] providers = new DefaultProvider[]{
                new DefaultProvider(9180),
                new DefaultProvider(9181),
                new DefaultProvider(9182)
        };

        CountDownLatch countDownLatch = new CountDownLatch(providers.length);

        for (Provider provider : providers) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    provider.start();
                    HelloService helloService = new HelloServiceImpl();
                    // 注册到本地容器 未发布到注册中心
                    ServiceWrapper serviceWrapper = provider.serviceRegistry()
                            .provider(helloService)
                            .interfaceClass(HelloService.class)
                            .providerName("org.rpc.example.demo.HelloService")
                            .group("test")
                            .version("1.0.0")
                            .register();
                    countDownLatch.countDown();
                }
            }).start();
        }

        countDownLatch.await();

    }
}
