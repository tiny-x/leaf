package com.leaf.example.demo.async;

import com.leaf.common.UnresolvedAddress;
import com.leaf.common.context.RpcContext;
import com.leaf.example.demo.HelloService;
import com.leaf.rpc.DefaultProxyFactory;
import com.leaf.rpc.consumer.DefaultLeafClient;
import com.leaf.rpc.consumer.InvokeType;
import com.leaf.rpc.consumer.LeafClient;
import com.leaf.rpc.consumer.future.InvokeFuture;
import com.leaf.rpc.consumer.future.InvokeFutureContext;
import com.leaf.rpc.consumer.future.InvokeFutureListener;

public class ConsumerAsyncExample {

    public static void main(String[] args) {
        LeafClient leafClient = new DefaultLeafClient("consumer");

        HelloService helloService = DefaultProxyFactory.factory(HelloService.class)
                .consumer(leafClient)
                .providers(new UnresolvedAddress("127.0.0.1", 9180))
                .group("test1")
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
