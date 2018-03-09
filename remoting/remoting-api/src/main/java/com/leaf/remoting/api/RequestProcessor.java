package com.leaf.remoting.api;

import com.leaf.remoting.api.payload.RequestCommand;
import com.leaf.remoting.api.payload.ResponseCommand;
import io.netty.channel.ChannelHandlerContext;


public interface RequestProcessor {

    ResponseCommand process(ChannelHandlerContext context, RequestCommand request);

    boolean rejectRequest();
}
