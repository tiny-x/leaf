package com.leaf.example.cluster.failover;

import com.leaf.common.UnresolvedAddress;
import com.leaf.example.cluster.api.ClusterService;
import com.leaf.rpc.ProxyFactory;
import com.leaf.rpc.consumer.Consumer;
import com.leaf.rpc.consumer.DefaultConsumer;
import com.leaf.rpc.consumer.InvokeType;
import com.leaf.rpc.consumer.cluster.ClusterInvoker;
import com.leaf.rpc.consumer.future.RpcContext;
import com.leaf.rpc.consumer.future.RpcFuture;
import com.leaf.rpc.consumer.future.RpcFutureListener;

import java.util.HashMap;

public class FailOverConsumer {

    public static void main(String[] args) {
        Consumer consumer = new DefaultConsumer("consumer");
        UnresolvedAddress[] addresses = new UnresolvedAddress[] {
                new UnresolvedAddress("127.0.0.1", 9180),
                new UnresolvedAddress("127.0.0.1", 9180),
                new UnresolvedAddress("127.0.0.1", 9181),
                new UnresolvedAddress("127.0.0.1", 9181),
                new UnresolvedAddress("127.0.0.1", 9182),
                new UnresolvedAddress("127.0.0.1", 9182),
        };
        for (UnresolvedAddress address : addresses) {
            consumer.connect(address);
            consumer.connect(address);
        }

        async(consumer, addresses);


    }

    private static void sync(Consumer consumer, UnresolvedAddress[] addresses) {
        ClusterService clusterService = ProxyFactory.factory(ClusterService.class)
                .consumer(consumer)
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

    private static void async(Consumer consumer, UnresolvedAddress[] addresses) {
        ClusterService clusterService = ProxyFactory.factory(ClusterService.class)
                .consumer(consumer)
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
            RpcFuture future = RpcContext.getFuture();
            future.addListener(new RpcFutureListener() {
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
            RpcFuture future = RpcContext.getFuture();
            future.addListener(new RpcFutureListener() {
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
            RpcFuture future = RpcContext.getFuture();
            future.addListener(new RpcFutureListener() {
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
