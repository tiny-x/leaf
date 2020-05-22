package com.leaf.spring.init.bean;

import com.leaf.register.api.RegisterType;
import com.leaf.rpc.consumer.LeafClient;
import com.leaf.rpc.consumer.DefaultLeafClient;
import org.springframework.beans.factory.InitializingBean;

public class ConsumerFactory implements InitializingBean {

    private String id;

    private RegisterType registerType;

    private String registryServer;

    private LeafClient leafClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        leafClient = new DefaultLeafClient(id, registerType);
        leafClient.connectToRegistryServer(registryServer);
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRegistryServer(String registryServer) {
        this.registryServer = registryServer;
    }

    public LeafClient getLeafClient() {
        return leafClient;
    }

    public void setRegisterType(String registerType) {
        this.registerType = RegisterType.parse(registerType);
        if (this.registerType == null) {
            throw new IllegalArgumentException("registerType:" + registerType);
        }
    }
}
