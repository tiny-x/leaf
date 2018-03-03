package com.leaf.example.demo.broadcast;

import com.leaf.common.UnresolvedAddress;
import com.leaf.common.model.ServiceMeta;
import com.leaf.example.demo.HelloService;
import com.leaf.remoting.netty.NettyClientConfig;
import com.leaf.rpc.ProxyFactory;
import com.leaf.rpc.consumer.Consumer;
import com.leaf.rpc.consumer.DefaultConsumer;
import com.leaf.rpc.consumer.InvokeType;
import com.leaf.rpc.consumer.dispatcher.DispatchType;
import com.leaf.rpc.consumer.future.InvokeFuture;
import com.leaf.rpc.consumer.future.RpcContext;
import com.leaf.rpc.consumer.future.RpcFutureGroup;
import com.leaf.rpc.consumer.future.RpcFutureListener;

public class ConsumerBroadcastExample {

    public static void main(String[] args) {
        NettyClientConfig config = new NettyClientConfig();
        Consumer consumer = new DefaultConsumer("consumer", config);
        UnresolvedAddress address = new UnresolvedAddress("127.0.0.1", 9180);
        UnresolvedAddress address2 = new UnresolvedAddress("127.0.0.1", 9181);
        UnresolvedAddress address3 = new UnresolvedAddress("127.0.0.1", 9182);
        consumer.connect(address);
        consumer.connect(address2);
        consumer.connect(address3);

        ServiceMeta serviceMeta = new ServiceMeta("test", "org.rpc.example.demo.HelloService", "1.0.0");
        consumer.client().addChannelGroup(serviceMeta, address);
        consumer.client().addChannelGroup(serviceMeta, address2);
        consumer.client().addChannelGroup(serviceMeta, address3);

        HelloService helloService = ProxyFactory.factory(HelloService.class)
                .consumer(consumer)
                .directory(serviceMeta)
                .timeMillis(3000L)
                .invokeType(InvokeType.ASYNC)
                .dispatcher(DispatchType.BROADCAST)
                .newProxy();

        String s = helloService.sayHello(" biu biu biu!!!");
        System.out.println(s);

        RpcFutureGroup rpcFutureGroup = RpcContext.getRpcFutureGroup();
        rpcFutureGroup.addListener(new RpcFutureListener<String>() {
            @Override
            public void complete(String result) {
                System.out.println("result: " + result);
            }

            @Override
            public void failure(Throwable cause) {
                System.out.println("error: " + cause);
            }
        });
        InvokeFuture[] futures = rpcFutureGroup.futures();

        for (InvokeFuture future : futures) {
            System.out.println("------>: " + future.get());
        }

    }
}
