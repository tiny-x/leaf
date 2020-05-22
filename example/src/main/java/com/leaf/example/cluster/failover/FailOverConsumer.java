package com.leaf.example.cluster.failover;

import com.leaf.common.UnresolvedAddress;
import com.leaf.example.cluster.api.ClusterService;
import com.leaf.rpc.DefaultProxyFactory;
import com.leaf.rpc.consumer.LeafClient;
import com.leaf.rpc.consumer.DefaultLeafClient;
import com.leaf.rpc.consumer.InvokeType;
import com.leaf.rpc.consumer.cluster.ClusterInvoker;
import com.leaf.rpc.consumer.future.InvokeFuture;
import com.leaf.rpc.consumer.future.InvokeFutureContext;
import com.leaf.rpc.consumer.future.InvokeFutureListener;

import java.util.HashMap;

public class FailOverConsumer {

    public static void main(String[] args) {
        LeafClient leafClient = new DefaultLeafClient("consumer");
        UnresolvedAddress[] addresses = new UnresolvedAddress[] {
                new UnresolvedAddress("127.0.0.1", 9180),
                new UnresolvedAddress("127.0.0.1", 9180),
                new UnresolvedAddress("127.0.0.1", 9181),
                new UnresolvedAddress("127.0.0.1", 9181),
                new UnresolvedAddress("127.0.0.1", 9182),
                new UnresolvedAddress("127.0.0.1", 9182),
        };
        for (UnresolvedAddress address : addresses) {
            leafClient.connect(address);
            leafClient.connect(address);
        }

        async(leafClient, addresses);


    }

    private static void sync(LeafClient leafClient, UnresolvedAddress[] addresses) {
        ClusterService clusterService = DefaultProxyFactory.factory(ClusterService.class)
                .consumer(leafClient)
                .providers(addresses)
                .strategy(ClusterInvoker.Strategy.FAIL_OVER)
                .retries(1)
                .newProxy();

        HashMap<String, Object> user = new HashMap<>();
        user.put("name", "hh");
        user.put("age", 10);

        try {
            clusterService.addUser(user);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            clusterService.getName();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            clusterService.getAge();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void async(LeafClient leafClient, UnresolvedAddress[] addresses) {
        ClusterService clusterService = DefaultProxyFactory.factory(ClusterService.class)
                .consumer(leafClient)
                .providers(addresses)
                .invokeType(InvokeType.ASYNC)
                .strategy(ClusterInvoker.Strategy.FAIL_OVER)
                .retries(1)
                .newProxy();

        HashMap<String, Object> user = new HashMap<>();
        user.put("name", "hh");
        user.put("age", 10);

        try {
            clusterService.addUser(user);
            InvokeFuture future = InvokeFutureContext.getInvokeFuture();
            future.addListener(new InvokeFutureListener() {
                @Override
                public void complete(Object result) {
                    System.out.println(result);
                }

                @Override
                public void failure(Throwable cause) {
                    cause.printStackTrace();
                }
            });
            System.out.println("addUser: " + future.get());
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clusterService.getAge();
            InvokeFuture future = InvokeFutureContext.getInvokeFuture();
            future.addListener(new InvokeFutureListener() {
                @Override
                public void complete(Object result) {
                    System.out.println(result);
                }

                @Override
                public void failure(Throwable cause) {
                    cause.printStackTrace();
                }
            });
            System.out.println("getAge: " + future.get());
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            clusterService.getName();
            InvokeFuture future = InvokeFutureContext.getInvokeFuture();
            future.addListener(new InvokeFutureListener() {
                @Override
                public void complete(Object result) {
                    System.out.println(result);
                }

                @Override
                public void failure(Throwable cause) {
                    cause.printStackTrace();
                }
            });
            System.out.println("getName: " + future.get());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
