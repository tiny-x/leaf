package com.leaf.rpc.provider;

import com.leaf.common.UnresolvedAddress;
import com.leaf.common.model.Directory;
import com.leaf.common.model.RegisterMeta;
import com.leaf.common.model.ServiceWrapper;
import com.leaf.common.utils.InetUtils;
import com.leaf.register.api.RegisterFactory;
import com.leaf.register.api.RegisterService;
import com.leaf.register.api.RegisterType;
import com.leaf.remoting.api.RpcServer;
import com.leaf.remoting.netty.NettyServer;
import com.leaf.remoting.netty.NettyServerConfig;
import com.leaf.rpc.container.DefaultServiceProviderContainer;
import com.leaf.rpc.container.ServiceProviderContainer;
import com.leaf.rpc.controller.FlowController;
import com.leaf.rpc.exector.ExectorFactory;
import com.leaf.rpc.exector.ThreadPoolExectorFactory;
import com.leaf.rpc.local.DefaultServiceRegistry;
import com.leaf.rpc.local.ServiceRegistry;
import com.leaf.rpc.provider.process.DefaultProviderProcessor;

public class DefaultProvider implements Provider {

    private RpcServer server;

    private ServiceProviderContainer serviceProviderContainer;

    private RegisterService registerService = null;

    private FlowController[] flowControllers;

    private NettyServerConfig config;

    private ExectorFactory exectorFactory = new ThreadPoolExectorFactory();


    public DefaultProvider(int port) {
        this(port, null);
    }

    public DefaultProvider(NettyServerConfig nettyServerConfig) {
        this(0, nettyServerConfig);
    }

    public DefaultProvider(int port, NettyServerConfig nettyServerConfig) {
        if (nettyServerConfig == null) {
            this.config = new NettyServerConfig();
        }
        if (port != 0) {
            this.config.setPort(port);
        }
        this.serviceProviderContainer = new DefaultServiceProviderContainer();
        this.server = new NettyServer(config);
    }
    
    @Override
    public void start() {
        this.server.start();
        this.server.registerRequestProcess(new DefaultProviderProcessor(this), exectorFactory.createExecutorService());
    }

    @Override
    public void connectToRegistryServer(String addressess) {
        registerService = RegisterFactory.registerService(RegisterType.DEFAULT);
        registerService.connectToRegistryServer(addressess);
    }

    @Override
    public ServiceWrapper lookupService(Directory directory) {
        return serviceProviderContainer.lookupService(directory.directory());
    }

    @Override
    public ServiceRegistry serviceRegistry() {
        return new DefaultServiceRegistry(serviceProviderContainer);
    }

    @Override
    public void registerGlobalFlowController(FlowController... flowControllers) {
        this.flowControllers = flowControllers;
    }

    @Override
    public FlowController[] globalFlowController() {
        return flowControllers;
    }

    @Override
    public void publishService(ServiceWrapper serviceWrapper) {

        RegisterMeta registerMeta = new RegisterMeta();
        registerMeta.setServiceMeta(serviceWrapper.getServiceMeta());
        registerMeta.setConnCount(config.getConnCount());
        registerMeta.setAddress(new UnresolvedAddress(InetUtils.getLocalHost(), config.getPort()));
        registerMeta.setWeight(serviceWrapper.getWeight());

        registerService.register(registerMeta);
    }


}
