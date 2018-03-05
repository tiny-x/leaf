package com.leaf.rpc.consumer;

import com.leaf.common.UnresolvedAddress;
import com.leaf.common.model.Directory;
import com.leaf.common.model.RegisterMeta;
import com.leaf.common.model.ServiceMeta;
import com.leaf.common.utils.AnyThrow;
import com.leaf.register.api.*;
import com.leaf.remoting.api.RpcClient;
import com.leaf.remoting.netty.NettyClient;
import com.leaf.remoting.netty.NettyClientConfig;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class DefaultConsumer implements Consumer {

    private String application;

    private RpcClient rpcClient;

    private RegisterService registerService = null;

    public DefaultConsumer(String application) {
       this(application, null);
    }

    public DefaultConsumer(String application, NettyClientConfig nettyClientConfig) {
        checkNotNull(application, "application");
        nettyClientConfig = nettyClientConfig == null ? new NettyClientConfig() : nettyClientConfig;
        this.application = application;
        this.rpcClient = new NettyClient(nettyClientConfig);
        this.rpcClient.start();
    }

    @Override
    public RpcClient client() {
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
        registerService.subscribe((ServiceMeta) directory, listener);
    }

    @Override
    public void offlineListening(UnresolvedAddress address, OfflineListener listener) {
        registerService.offlineListening(address, listener);
    }

    @Override
    public void connectToRegistryServer(String addressess) {
        registerService = RegisterFactory.registerService(RegisterType.DEFAULT);
        registerService.connectToRegistryServer(addressess);
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
