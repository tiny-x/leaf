package com.leaf.remoting.api;

import com.leaf.remoting.api.payload.RequestCommand;
import com.leaf.remoting.api.payload.ResponseCommand;
import io.netty.channel.ChannelHandlerContext;


public interface RequestCommandProcessor {

    ResponseCommand process(ChannelHandlerContext context, RequestCommand request);

    ResponseCommand process(ChannelHandlerContext context, RequestCommand request, Throwable e);

}
