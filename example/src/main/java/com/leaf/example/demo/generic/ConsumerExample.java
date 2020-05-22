package com.leaf.example.demo.generic;

import com.leaf.common.UnresolvedAddress;
import com.leaf.common.model.ServiceMeta;
import com.leaf.remoting.netty.NettyClientConfig;
import com.leaf.rpc.GenericProxyFactory;
import com.leaf.rpc.consumer.LeafClient;
import com.leaf.rpc.consumer.DefaultLeafClient;
import com.leaf.rpc.consumer.invoke.GenericInvoke;

public class ConsumerExample {

    public static void main(String[] args) throws Throwable {
        NettyClientConfig config = new NettyClientConfig();
        LeafClient leafClient = new DefaultLeafClient("consumer", config);
        UnresolvedAddress address = new UnresolvedAddress("127.0.0.1", 9180);
        leafClient.connect(address);

        ServiceMeta serviceMeta = new ServiceMeta("test", "org.rpc.example.demo.HelloService", "1.0.0");
        leafClient.client().addChannelGroup(serviceMeta, address);

        GenericInvoke genericInvoke = GenericProxyFactory.factory()
                .consumer(leafClient)
                .directory(serviceMeta)
                .timeMillis(3000L)
                .newProxy();

        User user = new User();
        user.setName("aaaa");
        user.setAge("18");
        String s = (String) genericInvoke.$invoke("sayHello", "yefei", "8");
        System.out.printf("---------->: receive provider message %s \n", s);

        GenericInvoke genericInvoke2 = GenericProxyFactory.factory()
                .consumer(leafClient)
                .directory(serviceMeta)
                .timeMillis(1L)
                .newProxy();
        String s2 = (String) genericInvoke2.$invoke("sayHello", "yefei", "18");
        System.out.printf("---------->: receive provider message %s \n", s2);

    }
}
