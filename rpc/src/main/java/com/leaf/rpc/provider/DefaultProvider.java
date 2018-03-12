package com.leaf.rpc.provider;

import com.leaf.common.UnresolvedAddress;
import com.leaf.common.constants.Constants;
import com.leaf.common.model.Directory;
import com.leaf.register.api.model.RegisterMeta;
import com.leaf.rpc.local.ServiceWrapper;
import com.leaf.common.utils.InetUtils;
import com.leaf.register.api.RegisterFactory;
import com.leaf.register.api.RegisterService;
import com.leaf.register.api.RegisterType;
import com.leaf.remoting.api.RemotingServer;
import com.leaf.remoting.netty.NettyServer;
import com.leaf.remoting.netty.NettyServerConfig;
import com.leaf.rpc.container.DefaultServiceProviderContainer;
import com.leaf.rpc.container.ServiceProviderContainer;
import com.leaf.rpc.controller.FlowController;
import com.leaf.rpc.exector.ExecutorFactory;
import com.leaf.rpc.exector.ThreadPoolExecutorFactory;
import com.leaf.rpc.local.DefaultServiceRegistry;
import com.leaf.rpc.local.ServiceRegistry;
import com.leaf.rpc.provider.process.DefaultProviderProcessor;

public class DefaultProvider implements Provider {

    private final RemotingServer server;

    private final ServiceProviderContainer serviceProviderContainer;

    private final NettyServerConfig config;

    private final ExecutorFactory executorFactory = new ThreadPoolExecutorFactory();

    private final RegisterType registerType;

    private RegisterService registerService = null;

    private FlowController[] flowControllers;

    public DefaultProvider() {
        this(Constants.DEFAULT_PROVIDER_PORT);
    }

    public DefaultProvider(int port) {
        this(port, new NettyServerConfig());
    }

    public DefaultProvider(NettyServerConfig nettyServerConfig) {
        this(Constants.DEFAULT_PROVIDER_PORT, nettyServerConfig);
    }

    public DefaultProvider(int port, NettyServerConfig nettyServerConfig) {
        this(port, nettyServerConfig, RegisterType.DEFAULT);
    }

    public DefaultProvider(int port, RegisterType registerType) {
        this(port, new NettyServerConfig(), registerType);
    }

    public DefaultProvider(int port, NettyServerConfig nettyServerConfig, RegisterType registerType) {
        this.config = nettyServerConfig;
        this.registerType = registerType;

        this.server = new NettyServer(config);
        this.serviceProviderContainer = new DefaultServiceProviderContainer();
        this.config.setPort(port);

    }

    @Override
    public void start() {
        this.server.start();
        this.server.registerRequestProcess(new DefaultProviderProcessor(this), executorFactory.createExecutorService());
    }

    @Override
    public void connectToRegistryServer(String addresses) {
        registerService = RegisterFactory.registerService(registerType);
        registerService.connectToRegistryServer(addresses);
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
