package com.leaf.example.spring;

import com.leaf.register.DefaultRegisterServer;
import com.leaf.register.RegisterServer;
import com.leaf.remoting.netty.NettyServerConfig;

public class RegisterServerExample {

    public static void main(String[] args) {
        NettyServerConfig config = new NettyServerConfig();
        config.setPort(9876);

        RegisterServer registerServer = new DefaultRegisterServer(config);
        registerServer.start();
    }
}
