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
import com.leaf.rpc.consumer.future.*;

public class ConsumerBroadcastExample {

    public static void main(String[] args) {
        NettyClientConfig config = new NettyClientConfig();
        Consumer consumer = new DefaultConsumer("consumer", config);
        UnresolvedAddress[] addresses = new UnresolvedAddress[] {
                new UnresolvedAddress("127.0.0.1", 9180),
                new UnresolvedAddress("127.0.0.1", 9180),
                new UnresolvedAddress("127.0.0.1", 9181),
                new UnresolvedAddress("127.0.0.1", 9181),
                new UnresolvedAddress("127.0.0.1", 9182),
                new UnresolvedAddress("127.0.0.1", 9182),
        };

        ServiceMeta serviceMeta = new ServiceMeta(
                "test",
                "org.rpc.example.demo.HelloService",
                "1.0.0");

        for (UnresolvedAddress address : addresses) {
            consumer.connect(address);
            consumer.client().addChannelGroup(serviceMeta, address);
        }

        HelloService helloService = ProxyFactory.factory(HelloService.class)
                .consumer(consumer)
                .directory(serviceMeta)
                .timeMillis(3000L)
                .invokeType(InvokeType.ASYNC)
                .dispatcher(DispatchType.BROADCAST)
                .newProxy();

        String s = helloService.sayHello(" biu biu biu!!!");
        System.out.println(s);

        InvokeFutureGroup invokeFutureGroup = InvokeFutureContext.getInvokeFutureGroup();
        invokeFutureGroup.addListener(new InvokeFutureListener<String>() {
            @Override
            public void complete(String result) {
                System.out.println("result: " + result);
            }

            @Override
            public void failure(Throwable cause) {
                System.out.println("error: " + cause);
            }
        });
        InvokeFuture[] futures = invokeFutureGroup.futures();

        for (InvokeFuture future : futures) {
            try {
                System.out.println("------>: " + future.get());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

    }
}
