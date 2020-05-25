package com.leaf.rpc.consumer;

import com.leaf.common.UnresolvedAddress;
import com.leaf.common.model.Directory;
import com.leaf.common.model.ServiceMeta;
import com.leaf.common.utils.AnyThrow;
import com.leaf.common.utils.InetUtils;
import com.leaf.register.api.*;
import com.leaf.register.api.model.SubscribeMeta;
import com.leaf.remoting.api.RemotingClient;
import com.leaf.remoting.netty.NettyClient;
import com.leaf.remoting.netty.NettyClientConfig;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author yefei
 */
public class DefaultLeafClient implements LeafClient {

    private final String application;

    private final RemotingClient remotingClient;

    private final RegisterType registerType;

    private RegisterService registerService = null;

    public DefaultLeafClient(String application) {
       this(application, new NettyClientConfig(), RegisterType.DEFAULT);
    }

    public DefaultLeafClient(String application, NettyClientConfig nettyClientConfig) {
        this(application, nettyClientConfig, RegisterType.DEFAULT);
    }

    public DefaultLeafClient(String application, RegisterType registerType) {
        this(application, new NettyClientConfig(), registerType);
    }

    public DefaultLeafClient(String application, NettyClientConfig nettyClientConfig, RegisterType registerType) {
        checkNotNull(application, "application");

        this.application = application;
        this.registerType = registerType;
        this.remotingClient = new NettyClient(nettyClientConfig);
        this.remotingClient.start();
    }

    @Override
    public RemotingClient remotingClient() {
        return remotingClient;
    }

    @Override
    public void connect(UnresolvedAddress address) {
        try {
            remotingClient.connect(address);
        } catch (Exception e) {
            AnyThrow.throwUnchecked(e);
        }
    }

    @Override
    public void subscribe(Directory directory, NotifyListener listener) {
        SubscribeMeta subscribeMeta = new SubscribeMeta();
        subscribeMeta.setServiceMeta((ServiceMeta) directory);
        subscribeMeta.setAddressHost(InetUtils.getLocalHost());
        registerService.subscribeRegisterMeta(subscribeMeta, listener);
    }

    @Override
    public void offlineListening(UnresolvedAddress address, OfflineListener listener) {
        registerService.offlineListening(address, listener);
    }

    @Override
    public void connectToRegistryServer(String addresses) {
        registerService = RegisterFactory.registerService(registerType);
        registerService.connectToRegistryServer(addresses);
    }

    @Override
    public void shutdown() {
        remotingClient.shutdownGracefully();
        if (registerService != null) {
            registerService.shutdown();
        }
    }

    @Override
    public String application() {
        return application;
    }

    @Override
    public RegisterService registerService() {
        return registerService;
    }
}
