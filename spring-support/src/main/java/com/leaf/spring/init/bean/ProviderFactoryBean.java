package com.leaf.spring.init.bean;

import com.google.common.base.Strings;
import com.leaf.register.api.RegisterType;
import com.leaf.rpc.provider.DefaultLeafServer;
import com.leaf.rpc.provider.LeafServer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

public class ProviderFactoryBean implements FactoryBean<LeafServer>, InitializingBean, DisposableBean {

    private Integer port;

    private RegisterType registerType;

    private String registryServer;

    private LeafServer leafServer;

    private String group;

    public ProviderFactoryBean() {

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (port == null && registerType == null) {
            leafServer = new DefaultLeafServer();
        } else if (port == null) {
            leafServer = new DefaultLeafServer(registerType);
        } else {
            leafServer = new DefaultLeafServer(port, registerType);
        }

        if (!Strings.isNullOrEmpty(registryServer)) {
            leafServer.connectToRegistryServer(registryServer);
        }
        leafServer.start();
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

    public LeafServer getLeafServer() {
        return leafServer;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setRegisterType(String registerType) {
        this.registerType = RegisterType.parse(registerType);
        if (this.registerType == null) {
            throw new IllegalArgumentException("registerType:" + registerType);
        }
    }

    @Override
    public void destroy() throws Exception {
        leafServer.shutdown();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public LeafServer getObject() throws Exception {
        return leafServer;
    }

    @Override
    public Class<?> getObjectType() {
        return LeafServer.class;
    }
}
