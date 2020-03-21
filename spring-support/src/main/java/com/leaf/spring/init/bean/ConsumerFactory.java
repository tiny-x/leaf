package com.leaf.spring.init.bean;

import com.leaf.register.api.RegisterType;
import com.leaf.rpc.consumer.Consumer;
import com.leaf.rpc.consumer.DefaultConsumer;
import org.springframework.beans.factory.InitializingBean;

public class ConsumerFactory implements InitializingBean {

    private String id;

    private RegisterType registerType;

    private String registryServer;

    private Consumer consumer;

    @Override
    public void afterPropertiesSet() throws Exception {
        consumer = new DefaultConsumer(id, registerType);
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

    public void setRegisterType(String registerType) {
        this.registerType = RegisterType.parse(registerType);
        if (this.registerType == null) {
            throw new IllegalArgumentException("registerType:" + registerType);
        }
    }
}
