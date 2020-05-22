package com.leaf.example.demo.basic;

import com.leaf.common.UnresolvedAddress;
import com.leaf.common.model.ServiceMeta;
import com.leaf.example.demo.HelloService;
import com.leaf.rpc.DefaultProxyFactory;
import com.leaf.rpc.consumer.LeafClient;
import com.leaf.rpc.consumer.DefaultLeafClient;

public class ConsumerExample {

    private static HelloService helloService;

    static {
        LeafClient leafClient = new DefaultLeafClient("consumer");
        UnresolvedAddress address = new UnresolvedAddress("127.0.0.1", 9180);
        leafClient.connect(address);
        leafClient.connect(address);
        leafClient.connect(address);
        leafClient.connect(address);

        ServiceMeta serviceMeta = new ServiceMeta("test", "org.rpc.example.demo.HelloService", "1.0.0");
        leafClient.client().addChannelGroup(serviceMeta, address);

        helloService = DefaultProxyFactory.factory(HelloService.class)
                .consumer(leafClient)
                .directory(serviceMeta)
                .timeMillis(300000L)
                .newProxy();
    }

    public static void main(String[] args) {
        new ConsumerExample().invoke();
    }

    public void invoke() {
        helloService.sayHello(" biu biu biu!!!");
    }
}
