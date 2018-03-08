package com.leaf.spring.init.bean;

import com.google.common.base.Strings;
import com.leaf.remoting.netty.NettyServerConfig;
import com.leaf.rpc.provider.DefaultProvider;
import com.leaf.rpc.provider.Provider;
import org.springframework.beans.factory.InitializingBean;

public class SpringProvider implements InitializingBean {

    private Integer port;

    private String registryServer;

    private Provider provider;

    public SpringProvider() {

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        NettyServerConfig config = new NettyServerConfig();
        if (port != null) {
            config.setPort(this.port);
        }
        provider = new DefaultProvider(config);
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
}
