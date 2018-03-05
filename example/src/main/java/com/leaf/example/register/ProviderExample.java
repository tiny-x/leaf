package com.leaf.example.register;

import com.leaf.common.model.ServiceWrapper;
import com.leaf.rpc.provider.DefaultProvider;
import com.leaf.rpc.provider.Provider;

import java.util.concurrent.CountDownLatch;

public class ProviderExample {

    public static void main(String[] args) throws InterruptedException {

        HelloService helloService = new HelloServiceImpl();

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
                    // 注册到本地容器 未发布到注册中心
                    ServiceWrapper serviceWrapper = provider.serviceRegistry()
                            .provider(helloService)
                            .register();
                    provider.connectToRegistryServer("127.0.0.1:9876");
                    provider.publishService(serviceWrapper);
                }
            }).start();
        }
        countDownLatch.await();

    }
}
