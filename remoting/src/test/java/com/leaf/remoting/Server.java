package com.leaf.remoting;

import com.leaf.remoting.api.ChannelEventListener;
import com.leaf.remoting.api.RpcServer;
import com.leaf.remoting.netty.NettyServer;
import com.leaf.remoting.netty.NettyServerConfig;
import io.netty.channel.Channel;


public class Server {

    public static void main(String[] args) {
        RpcServer server = new NettyServer(new NettyServerConfig(), new ChannelEventListener() {
            @Override
            public void onChannelConnect(String remoteAddr, Channel channel) {
                System.out.println("onChannelConnect");
            }

            @Override
            public void onChannelClose(String remoteAddr, Channel channel) {
                System.out.println("onChannelClose");
            }

            @Override
            public void onChannelException(String remoteAddr, Channel channel) {

            }

            @Override
            public void onChannelIdle(String remoteAddr, Channel channel) {

            }

            @Override
            public void onChannelActive(String remoteAddr, Channel channel) {

            }

            @Override
            public void onChannelInActive(String remoteAddr, Channel channel) {

            }
        });

        server.start();
    }
}
