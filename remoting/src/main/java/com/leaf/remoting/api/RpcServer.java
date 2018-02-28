package com.leaf.remoting.api;

import com.leaf.remoting.api.payload.RequestCommand;
import com.leaf.remoting.api.payload.ResponseCommand;
import com.leaf.remoting.exception.RemotingException;
import io.netty.channel.Channel;


import java.util.concurrent.ExecutorService;

public interface RpcServer extends RpcService {

    void invokeSync(final Channel channel, final RequestCommand request, long timeoutMillis) throws RemotingException, InterruptedException;

    void invokeAsync(final Channel channel, final RequestCommand request
            , long timeoutMillis, InvokeCallback<ResponseCommand> invokeCallback) throws RemotingException, InterruptedException;

    void invokeOneWay(final Channel channel, final RequestCommand request, long timeoutMillis)
            throws RemotingException, InterruptedException;

    void registerRequestProcess(RequestProcessor requestProcessor, ExecutorService executor);

}
