package com.leaf.example.cluster;

import com.leaf.common.model.ServiceWrapper;
import com.leaf.example.cluster.api.ClusterService;
import com.leaf.example.cluster.api.FailServiceImpl;
import com.leaf.rpc.provider.DefaultProvider;
import com.leaf.rpc.provider.Provider;

import java.util.concurrent.CountDownLatch;

public class ClusterProviderExample {

    public static void main(String[] args) throws InterruptedException {

        ClusterService clusterService = new FailServiceImpl();

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
                            .provider(clusterService)
                            .register();
                }
            }).start();
        }
        countDownLatch.await();

    }
}
