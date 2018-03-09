package com.leaf.remoting.api;

import io.netty.channel.Channel;

public abstract class ChannelEventAdapter implements ChannelEventListener {

    @Override
    public void onChannelConnect(String remoteAddr, Channel channel) {

    }

    @Override
    public void onChannelActive(String remoteAddr, Channel channel) {

    }

    @Override
    public void onChannelInActive(String remoteAddr, Channel channel) {

    }

    @Override
    public void onChannelClose(String remoteAddr, Channel channel) {

    }

    @Override
    public void onChannelException(String remoteAddr, Channel channel) {

    }

    @Override
    public void onChannelIdle(String remoteAddr, Channel channel) {

    }
}
