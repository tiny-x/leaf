package com.leaf.spring.init.bean;

import com.leaf.register.api.RegisterType;
import com.leaf.rpc.consumer.LeafClient;
import com.leaf.rpc.consumer.DefaultLeafClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 *
 */
public class ConsumerFactory implements FactoryBean<LeafClient>, InitializingBean, DisposableBean {

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

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public LeafClient getObject() throws Exception {
        return leafClient;
    }

    @Override
    public Class<?> getObjectType() {
        return LeafClient.class;
    }

    @Override
    public void destroy() throws Exception {
        leafClient.shutdown();
    }
}
