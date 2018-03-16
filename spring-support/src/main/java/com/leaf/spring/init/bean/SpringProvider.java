package com.leaf.spring.init.bean;

import com.google.common.base.Strings;
import com.leaf.register.api.RegisterType;
import com.leaf.rpc.provider.DefaultProvider;
import com.leaf.rpc.provider.Provider;
import org.springframework.beans.factory.InitializingBean;

public class SpringProvider implements InitializingBean {

    private Integer port;

    private RegisterType registerType;

    private String registryServer;

    private Provider provider;

    public SpringProvider() {

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        provider = new DefaultProvider(port, registerType);
        if (!Strings.isNullOrEmpty(registryServer)) {
            provider.connectToRegistryServer(registryServer);
        }
        provider.start();
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getRegistryServer() {
        return registryServer;
    }

    public void setRegistryServer(String registryServer) {
        this.registryServer = registryServer;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setRegisterType(String registerType) {
        this.registerType = RegisterType.parse(registerType);
        if (this.registerType == null) {
            throw new IllegalArgumentException("registerType:" + registerType);
        }
    }
}
