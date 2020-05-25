package com.leaf.rpc.provider;

import com.leaf.common.UnresolvedAddress;
import com.leaf.common.constants.Constants;
import com.leaf.common.model.Directory;
import com.leaf.common.utils.InetUtils;
import com.leaf.register.api.RegisterFactory;
import com.leaf.register.api.RegisterService;
import com.leaf.register.api.RegisterType;
import com.leaf.register.api.model.RegisterMeta;
import com.leaf.remoting.api.RemotingServer;
import com.leaf.remoting.netty.NettyServer;
import com.leaf.remoting.netty.NettyServerConfig;
import com.leaf.rpc.container.DefaultServiceProviderContainer;
import com.leaf.rpc.container.ServiceProviderContainer;
import com.leaf.rpc.controller.FlowController;
import com.leaf.rpc.exector.DefaultThreadFactory;
import com.leaf.rpc.exector.ExecutorFactory;
import com.leaf.rpc.exector.ThreadPoolExecutorFactory;
import com.leaf.rpc.local.DefaultServiceRegistry;
import com.leaf.rpc.local.ServiceRegistry;
import com.leaf.rpc.local.ServiceWrapper;
import com.leaf.rpc.provider.process.DefaultRequestProcessor;
import com.leaf.rpc.provider.process.RequestProcessFilter;
import com.leaf.rpc.provider.process.RequestProcessor;

import java.lang.reflect.Method;

/**
 * @author yefei
 */
public class DefaultLeafServer implements LeafServer {

    private final RemotingServer server;

    private final ServiceProviderContainer serviceProviderContainer;

    private final NettyServerConfig config;

    private final ExecutorFactory executorFactory = new ThreadPoolExecutorFactory();

    private final RegisterType registerType;

    private RegisterService registerService = null;

    private FlowController[] flowControllers;

    private volatile RequestProcessor requestProcessor;

    public DefaultLeafServer() {
        this(Constants.DEFAULT_PROVIDER_PORT);
    }

    public DefaultLeafServer(int port) {
        this(port, new NettyServerConfig());
    }

    public DefaultLeafServer(NettyServerConfig nettyServerConfig) {
        this(Constants.DEFAULT_PROVIDER_PORT, nettyServerConfig);
    }

    public DefaultLeafServer(RegisterType registerType) {
        this(Constants.DEFAULT_PROVIDER_PORT, registerType);
    }

    public DefaultLeafServer(int port, NettyServerConfig nettyServerConfig) {
        this(port, nettyServerConfig, RegisterType.DEFAULT);
    }

    public DefaultLeafServer(int port, RegisterType registerType) {
        this(port, new NettyServerConfig(), registerType);
    }

    public DefaultLeafServer(int port, NettyServerConfig nettyServerConfig, RegisterType registerType) {
        this.config = nettyServerConfig;
        this.registerType = registerType;

        this.server = new NettyServer(config);
        this.serviceProviderContainer = new DefaultServiceProviderContainer();
        this.requestProcessor = new DefaultRequestProcessor(serviceProviderContainer);
        this.server.registerRequestProcess(requestProcessor.requestCommandProcessor(), executorFactory.createExecutorService(new DefaultThreadFactory()));
        this.config.setPort(port);
    }

    @Override
    public void start() {
        this.server.start();
    }

    @Override
    public void shutdown() {
        if (registerService != null) {
            registerService.shutdown();
        }
        server.shutdownGracefully();
    }

    @Override
    public String application() {
        return null;
    }

    @Override
    public void addRequestProcessFilter(RequestProcessFilter requestProcessFilter) {
        requestProcessor.addRequestProcessFilter(requestProcessFilter);
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

        Class<?> anInterface = serviceWrapper.getServiceProvider().getClass().getInterfaces()[0];
        Method[] declaredMethods = anInterface.getDeclaredMethods();
        String[] methods = new String[declaredMethods.length];
        for (int i = 0; i < declaredMethods.length; i++) {
            methods[i] = declaredMethods[i].getName();
        }
        registerMeta.setMethods(methods);

        registerService.register(registerMeta);
    }
}
