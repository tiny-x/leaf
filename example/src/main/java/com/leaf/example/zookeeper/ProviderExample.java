package com.leaf.example.zookeeper;

import com.leaf.rpc.local.ServiceWrapper;
import com.leaf.example.register.HelloService;
import com.leaf.example.register.HelloServiceImpl;
import com.leaf.register.api.RegisterType;
import com.leaf.rpc.provider.DefaultProvider;
import com.leaf.rpc.provider.Provider;

import java.util.concurrent.CountDownLatch;

public class ProviderExample {

    public static void main(String[] args) throws InterruptedException {

        HelloService helloService = new HelloServiceImpl();

        Provider[] providers = new DefaultProvider[]{
                new DefaultProvider(9180, RegisterType.ZOOKEEPER),
                new DefaultProvider(9181, RegisterType.ZOOKEEPER),
                new DefaultProvider(9182, RegisterType.ZOOKEEPER)
        };

        CountDownLatch countDownLatch = new CountDownLatch(providers.length);

        for (Provider provider : providers) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    provider.start();
                    // 注册到本地容器 未发布到注册中心
                    ServiceWrapper serviceWrapper = provider.serviceRegistry()
                            .provider(helloService)
                            .register();
                    provider.connectToRegistryServer("127.0.0.1:2181");
                    provider.publishService(serviceWrapper);
                    countDownLatch.countDown();
                }
            }).start();
            Thread.sleep(500);
        }
        countDownLatch.await();

    }
}
