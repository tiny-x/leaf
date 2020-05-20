package com.leaf.example.zookeeper;

import com.leaf.rpc.local.ServiceWrapper;
import com.leaf.example.register.HelloService;
import com.leaf.example.register.HelloServiceImpl;
import com.leaf.register.api.RegisterType;
import com.leaf.rpc.provider.DefaultLeafServer;
import com.leaf.rpc.provider.LeafServer;

import java.util.concurrent.CountDownLatch;

public class ProviderExample {

    public static void main(String[] args) throws InterruptedException {

        HelloService helloService = new HelloServiceImpl();

        LeafServer[] leafServers = new DefaultLeafServer[]{
                new DefaultLeafServer(9180, RegisterType.ZOOKEEPER),
                new DefaultLeafServer(9181, RegisterType.ZOOKEEPER),
                new DefaultLeafServer(9182, RegisterType.ZOOKEEPER)
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
                    leafServer.connectToRegistryServer("121.43.175.216:2181");
                    leafServer.publishService(serviceWrapper);
                    countDownLatch.countDown();
                }
            }).start();
            Thread.sleep(500);
        }
        countDownLatch.await();

    }
}
