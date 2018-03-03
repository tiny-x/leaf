package com.leaf.example.demo.oneway;

import com.leaf.common.UnresolvedAddress;
import com.leaf.common.model.ServiceMeta;
import com.leaf.example.demo.HelloService;
import com.leaf.remoting.netty.NettyClientConfig;
import com.leaf.rpc.ProxyFactory;
import com.leaf.rpc.consumer.Consumer;
import com.leaf.rpc.consumer.DefaultConsumer;
import com.leaf.rpc.consumer.InvokeType;

public class ConsumerOneWayExample {

    public static void main(String[] args) {
        NettyClientConfig config = new NettyClientConfig();
        Consumer consumer = new DefaultConsumer("consumer", config);
        UnresolvedAddress address = new UnresolvedAddress("127.0.0.1", 9180);
        consumer.connect(address);

        ServiceMeta serviceMeta = new ServiceMeta("test", "org.rpc.example.demo.HelloService", "1.0.0");
        consumer.client().addChannelGroup(serviceMeta, address);

        HelloService helloService = ProxyFactory.factory(HelloService.class)
                .consumer(consumer)
                .directory(serviceMeta)
                .timeMillis(3000L)
                .invokeType(InvokeType.ONE_WAY)
                .newProxy();

        String s = helloService.sayHello(" biu biu biu!!!");
        System.out.println(s);
    }
}
