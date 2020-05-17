package com.leaf.example.demo.broadcast;

import com.leaf.rpc.local.ServiceWrapper;
import com.leaf.example.demo.HelloService;
import com.leaf.example.demo.HelloServiceImpl;
import com.leaf.rpc.provider.DefaultLeafServer;
import com.leaf.rpc.provider.LeafServer;

import java.util.concurrent.CountDownLatch;

public class ProviderExample {

    public static void main(String[] args) throws InterruptedException {

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
                    HelloService helloService = new HelloServiceImpl();
                    // 注册到本地容器 未发布到注册中心
                    ServiceWrapper serviceWrapper = leafServer.serviceRegistry()
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
