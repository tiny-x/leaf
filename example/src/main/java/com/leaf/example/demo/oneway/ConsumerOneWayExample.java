package com.leaf.example.demo.oneway;

import com.leaf.common.UnresolvedAddress;
import com.leaf.common.model.ServiceMeta;
import com.leaf.example.demo.HelloService;
import com.leaf.remoting.netty.NettyClientConfig;
import com.leaf.rpc.DefaultProxyFactory;
import com.leaf.rpc.consumer.DefaultLeafClient;
import com.leaf.rpc.consumer.LeafClient;
import com.leaf.rpc.consumer.InvokeType;

public class ConsumerOneWayExample {

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
                .invokeType(InvokeType.ONE_WAY)
                .newProxy();

        String s = helloService.sayHello(" biu biu biu!!!");
        System.out.println(s);
    }
}
