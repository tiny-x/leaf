package com.leaf.register;

import com.leaf.register.process.RegisterProcess;
import com.leaf.remoting.api.RemotingServer;
import com.leaf.remoting.netty.NettyServer;
import com.leaf.remoting.netty.NettyServerConfig;

import java.util.concurrent.Executors;

public class DefaultRegisterServer implements RegisterServer {

    private static final int DEFAULT_PORT = 9876;

    private final RemotingServer rpcServer;

    private final NettyServerConfig nettyServerConfig;

    private final RegisterProcess requestProcessor;

    public DefaultRegisterServer() {
        this(new NettyServerConfig());
    }

    public DefaultRegisterServer(NettyServerConfig config) {
        config.setPort(DEFAULT_PORT);
        this.nettyServerConfig = config;
        this.requestProcessor  = new RegisterProcess(this);
        this.rpcServer = new NettyServer(config, requestProcessor.new RegisterChannelEventProcess());
        this.rpcServer.registerRequestProcess(requestProcessor, Executors.newCachedThreadPool());
    }

    @Override
    public void start() {
        rpcServer.start();
    }

    public RemotingServer getRpcServer() {
        return rpcServer;
    }

    public NettyServerConfig getNettyServerConfig() {
        return nettyServerConfig;
    }

    @Override
    public void shutdownGracefully() {
        requestProcessor.shutdownGracefully();
    }
}
