package com.leaf.remoting.netty.event;

public enum  ChannelEventType {
    ACTIVE,
    INACTIVE,
    CONNECT,
    CLOSE,
    ALL_IDLE,
    WRITE_IDLE,
    READ_IDLE,
    EXCEPTION
}
