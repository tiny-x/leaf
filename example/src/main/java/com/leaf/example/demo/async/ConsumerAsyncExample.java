package com.leaf.example.demo.async;

import com.leaf.common.UnresolvedAddress;
import com.leaf.common.context.RpcContext;
import com.leaf.common.model.ServiceMeta;
import com.leaf.example.demo.HelloService;
import com.leaf.remoting.netty.NettyClientConfig;
import com.leaf.rpc.DefaultProxyFactory;
import com.leaf.rpc.consumer.LeafClient;
import com.leaf.rpc.consumer.DefaultLeafClient;
import com.leaf.rpc.consumer.InvokeType;
import com.leaf.rpc.consumer.future.InvokeFuture;
import com.leaf.rpc.consumer.future.InvokeFutureContext;
import com.leaf.rpc.consumer.future.InvokeFutureListener;

public class ConsumerAsyncExample {

    public static void main(String[] args) {
        NettyClientConfig config = new NettyClientConfig();
        LeafClient leafClient = new DefaultLeafClient("consumer", config);
        UnresolvedAddress address = new UnresolvedAddress("127.0.0.1", 9180);
        leafClient.connect(address);

        ServiceMeta serviceMeta = new ServiceMeta("test", "org.rpc.example.demo.HelloService", "1.0.0");
        leafClient.client().addChannelGroup(serviceMeta, address);

        HelloService helloService = DefaultProxyFactory.factory(HelloService.class)
                .consumer(leafClient)
                .directory(serviceMeta)
                .timeMillis(3000L)
                .invokeType(InvokeType.ASYNC)
                .newProxy();

        RpcContext.putAttachment("attach", "async 我是 attach");
        String s = helloService.sayHello(" biu biu biu!!!");
        System.out.println(s);

        InvokeFuture<String> future = InvokeFutureContext.getInvokeFuture();
        future.addListener(new InvokeFutureListener<String>() {
            @Override
            public void complete(String result) {
                System.out.println("result: " + result);
            }

            @Override
            public void failure(Throwable cause) {
                System.out.println("error: " + cause);
            }
        });
        try {
            System.out.println(future.get());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

    }
}
