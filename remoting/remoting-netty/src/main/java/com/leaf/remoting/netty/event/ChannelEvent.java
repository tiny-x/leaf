package com.leaf.remoting.netty.event;

import io.netty.channel.Channel;

public class ChannelEvent {

    private final ChannelEventType type;
    private final String remoteAddr;
    private final Channel channel;

    public ChannelEvent(ChannelEventType type, String remoteAddr, Channel channel) {
        this.type = type;
        this.remoteAddr = remoteAddr;
        this.channel = channel;
    }

    public ChannelEventType getType() {
        return type;
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public Channel getChannel() {
        return channel;
    }

    @Override
    public String toString() {
        return "ChannelEvent [type=" + type + ", remoteAddr=" + remoteAddr + ", channel=" + channel + "]";
    }
}
