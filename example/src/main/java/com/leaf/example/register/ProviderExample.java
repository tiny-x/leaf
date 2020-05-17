package com.leaf.example.register;

import com.leaf.rpc.local.ServiceWrapper;
import com.leaf.rpc.provider.DefaultLeafServer;
import com.leaf.rpc.provider.LeafServer;

import java.util.concurrent.CountDownLatch;

public class ProviderExample {

    public static void main(String[] args) throws InterruptedException {

        HelloService helloService = new HelloServiceImpl();

        LeafServer[] leafServers = new DefaultLeafServer[]{
                new DefaultLeafServer(9180),
                new DefaultLeafServer(9181),
                new DefaultLeafServer(9182)
        };

        CountDownLatch countDownLatch = new CountDownLatch(leafServers.length);

        for (LeafServer leafServer : leafServers) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    leafServer.start();
                    // 注册到本地容器 未发布到注册中心
                    ServiceWrapper serviceWrapper = leafServer.serviceRegistry()
                            .provider(helloService)
                            .register();
                    leafServer.connectToRegistryServer("127.0.0.1:9876");
                    leafServer.publishService(serviceWrapper);
                }
            }).start();
        }
        countDownLatch.await();

    }
}
