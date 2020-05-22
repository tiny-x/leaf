package com.leaf.remoting.api;

import com.leaf.remoting.api.exception.RemotingException;
import com.leaf.remoting.api.payload.RequestCommand;
import com.leaf.remoting.api.payload.ResponseCommand;
import io.netty.channel.Channel;

/**
 * 传输层 服务端
 *
 * @author yefei
 */
public interface RemotingServer extends RemotingService {

    /**
     * 同步调用
     *
     * @param channel
     * @param request
     * @param timeoutMillis
     * @return
     * @throws RemotingException
     * @throws InterruptedException
     */
    ResponseCommand invokeSync(final Channel channel, final RequestCommand request, long timeoutMillis)
            throws RemotingException, InterruptedException;

    /**
     * 异步 callback
     * @param channel
     * @param request
     * @param timeoutMillis
     * @param invokeCallback
     * @throws RemotingException
     * @throws InterruptedException
     */
    void invokeAsync(final Channel channel, final RequestCommand request
            , long timeoutMillis, InvokeCallback<ResponseCommand> invokeCallback)
            throws RemotingException, InterruptedException;

    /**
     * 单步 不关系响应
     *
     * @param channel
     * @param request
     * @param timeoutMillis
     * @throws RemotingException
     * @throws InterruptedException
     */
    void invokeOneWay(final Channel channel, final RequestCommand request, long timeoutMillis)
            throws RemotingException, InterruptedException;
}
