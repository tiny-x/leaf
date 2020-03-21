package com.leaf.example.flow;

import com.leaf.common.UnresolvedAddress;
import com.leaf.common.model.ServiceMeta;
import com.leaf.remoting.api.exception.RemotingConnectException;
import com.leaf.remoting.netty.NettyClientConfig;
import com.leaf.rpc.DefaultProxyFactory;
import com.leaf.rpc.consumer.Consumer;
import com.leaf.rpc.consumer.DefaultConsumer;

public class ConsumerExample {

    public static void main(String[] args) throws RemotingConnectException, InterruptedException {
        NettyClientConfig config = new NettyClientConfig();
        Consumer consumer = new DefaultConsumer("consumer", config);
        UnresolvedAddress address = new UnresolvedAddress("127.0.0.1", 9180);
        consumer.connect(address);

        ServiceMeta serviceMeta = new ServiceMeta("test", "org.rpc.example.demo.HelloService", "1.0.0");
        consumer.client().addChannelGroup(serviceMeta, address);

        HelloService helloService = DefaultProxyFactory.factory(HelloService.class)
                .consumer(consumer)
                .directory(serviceMeta)
                .timeMillis(10000L)
                .newProxy();

        long l = System.currentTimeMillis();
        for (int i = 0; i < 101; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String s = helloService.sayHello(" biu biu biu!!!");
                    System.out.printf("---------->: receive provider message %s \n", s);
                }
            }).start();
        }
        System.out.printf("耗时 %s \n", System.currentTimeMillis() - l);

    }
}
