package com.leaf.rpc.consumer;

import com.leaf.common.UnresolvedAddress;
import com.leaf.common.model.Directory;
import com.leaf.common.utils.InetUtils;
import com.leaf.register.api.model.RegisterMeta;
import com.leaf.common.model.ServiceMeta;
import com.leaf.common.utils.AnyThrow;
import com.leaf.register.api.*;
import com.leaf.register.api.model.SubscribeMeta;
import com.leaf.remoting.api.RemotingClient;
import com.leaf.remoting.netty.NettyClient;
import com.leaf.remoting.netty.NettyClientConfig;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class DefaultConsumer implements Consumer {

    private final String application;

    private final RemotingClient rpcClient;

    private final RegisterType registerType;

    private RegisterService registerService = null;

    public DefaultConsumer(String application) {
       this(application, new NettyClientConfig(), RegisterType.DEFAULT);
    }

    public DefaultConsumer(String application, NettyClientConfig nettyClientConfig) {
        this(application, nettyClientConfig, RegisterType.DEFAULT);
    }

    public DefaultConsumer(String application, RegisterType registerType) {
        this(application, new NettyClientConfig(), registerType);
    }

    public DefaultConsumer(String application, NettyClientConfig nettyClientConfig, RegisterType registerType) {
        checkNotNull(application, "application");

        this.application = application;
        this.registerType = registerType;
        this.rpcClient = new NettyClient(nettyClientConfig);
        this.rpcClient.start();
    }

    @Override
    public RemotingClient client() {
        return rpcClient;
    }

    @Override
    public void connect(UnresolvedAddress address) {
        try {
            rpcClient.connect(address);
        } catch (Exception e) {
            AnyThrow.throwUnchecked(e);
        }
    }

    @Override
    public void subscribe(Directory directory, NotifyListener listener) {
        SubscribeMeta subscribeMeta = new SubscribeMeta();
        subscribeMeta.setServiceMeta((ServiceMeta) directory);
        subscribeMeta.setAddressHost(InetUtils.getLocalHost());
        registerService.subscribe(subscribeMeta, listener);
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
    public List<RegisterMeta> lookup(RegisterMeta registerMeta) {
        checkNotNull(registerService, "please connectToRegistryServer!");
        List<RegisterMeta> registerMetas = registerService.lookup(registerMeta);
        return registerMetas;
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
