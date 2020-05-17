package com.leaf.example.cluster;

import com.leaf.rpc.local.ServiceWrapper;
import com.leaf.example.cluster.api.ClusterService;
import com.leaf.example.cluster.api.FailServiceImpl;
import com.leaf.rpc.provider.DefaultLeafServer;
import com.leaf.rpc.provider.LeafServer;

import java.util.concurrent.CountDownLatch;

public class ClusterProviderExample {

    public static void main(String[] args) throws InterruptedException {

        ClusterService clusterService = new FailServiceImpl();

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
                            .provider(clusterService)
                            .register();
                }
            }).start();
        }
        countDownLatch.await();

    }
}
