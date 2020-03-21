package com.leaf.example.cluster.failsafe;

import com.leaf.common.UnresolvedAddress;
import com.leaf.example.cluster.api.ClusterService;
import com.leaf.rpc.DefaultProxyFactory;
import com.leaf.rpc.consumer.Consumer;
import com.leaf.rpc.consumer.DefaultConsumer;
import com.leaf.rpc.consumer.InvokeType;
import com.leaf.rpc.consumer.cluster.ClusterInvoker;
import com.leaf.rpc.consumer.future.InvokeFuture;
import com.leaf.rpc.consumer.future.InvokeFutureContext;
import com.leaf.rpc.consumer.future.InvokeFutureListener;

import java.util.HashMap;

public class FailSafeConsumer {

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

        System.out.println("------------------同步调用");
        sync(consumer, addresses);
        System.out.println("------------------异步调用");
        async(consumer, addresses);

    }

    private static void sync(Consumer consumer, UnresolvedAddress[] addresses) {
        ClusterService clusterService = DefaultProxyFactory.factory(ClusterService.class)
                .consumer(consumer)
                .providers(addresses)
                .strategy(ClusterInvoker.Strategy.FAIL_SAFE)
                .newProxy();

        HashMap<String, Object> user = new HashMap<>();
        user.put("name", "hh");
        user.put("age", 10);

        try {
            clusterService.addUser(user);
            System.out.println("addUser result: ");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            String name = clusterService.getName();
            System.out.println("getName result: " + name);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            int age = clusterService.getAge();
            System.out.println("getAge result: " + age);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void async(Consumer consumer, UnresolvedAddress[] addresses) {
        ClusterService clusterService = DefaultProxyFactory.factory(ClusterService.class)
                .consumer(consumer)
                .providers(addresses)
                .invokeType(InvokeType.ASYNC)
                .strategy(ClusterInvoker.Strategy.FAIL_SAFE)
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
