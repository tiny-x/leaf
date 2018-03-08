package com.leaf.spring.init.bean;

import com.leaf.remoting.netty.NettyClientConfig;
import com.leaf.rpc.consumer.Consumer;
import com.leaf.rpc.consumer.DefaultConsumer;
import org.springframework.beans.factory.InitializingBean;

public class SpringConsumer implements InitializingBean {

    private String id;

    private String registryServer;

    private Consumer consumer;

    @Override
    public void afterPropertiesSet() throws Exception {
        NettyClientConfig nettyClientConfig = new NettyClientConfig();
        consumer = new DefaultConsumer(id, nettyClientConfig);
        consumer.connectToRegistryServer(registryServer);
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRegistryServer(String registryServer) {
        this.registryServer = registryServer;
    }

    public Consumer getConsumer() {
        return consumer;
    }
}
