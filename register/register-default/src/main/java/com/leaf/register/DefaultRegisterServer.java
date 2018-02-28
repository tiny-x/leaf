package com.leaf.register;

import com.leaf.register.process.RegisterProcess;
import com.leaf.remoting.api.RpcServer;
import com.leaf.remoting.netty.NettyServer;
import com.leaf.remoting.netty.NettyServerConfig;

import java.util.concurrent.Executors;

public class DefaultRegisterServer implements RegisterServer {

    private static final int DEFAULT_PORT = 9876;

    private RpcServer rpcServer;

    public DefaultRegisterServer() {
        this(new NettyServerConfig());
    }

    public DefaultRegisterServer(NettyServerConfig config) {
        config.setPort(DEFAULT_PORT);
        RegisterProcess requestProcessor = new RegisterProcess();

        this.rpcServer = new NettyServer(config, requestProcessor.new RegisterChannelEventProcess());
        this.rpcServer.registerRequestProcess(requestProcessor, Executors.newCachedThreadPool());
    }

    @Override
    public void start() {
        rpcServer.start();
    }

}
