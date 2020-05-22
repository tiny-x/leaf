package com.leaf.remoting.api;

import com.leaf.common.UnresolvedAddress;
import com.leaf.common.model.Directory;
import com.leaf.remoting.api.channel.ChannelGroup;
import com.leaf.remoting.api.exception.RemotingConnectException;
import com.leaf.remoting.api.exception.RemotingException;
import com.leaf.remoting.api.payload.RequestCommand;
import com.leaf.remoting.api.payload.ResponseCommand;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author yefei
 */
public interface RemotingClient extends RemotingService {

    /**
     * 同步调用
     *
     * @param address
     * @param request
     * @param timeoutMillis
     * @return
     * @throws RemotingException
     * @throws InterruptedException
     */
    ResponseCommand invokeSync(final UnresolvedAddress address, final RequestCommand request, long timeoutMillis)
            throws RemotingException, InterruptedException;

    /**
     * 异步 callback
     * @param address
     * @param request
     * @param timeoutMillis
     * @param invokeCallback
     * @throws RemotingException
     * @throws InterruptedException
     */
    void invokeAsync(final UnresolvedAddress address, final RequestCommand request
            , long timeoutMillis, InvokeCallback<ResponseCommand> invokeCallback)
            throws RemotingException, InterruptedException;

    /**
     * 单步 不关系响应
     *
     * @param address
     * @param request
     * @param timeoutMillis
     * @throws RemotingException
     * @throws InterruptedException
     */
    void invokeOneWay(final UnresolvedAddress address, final RequestCommand request, long timeoutMillis)
            throws RemotingException, InterruptedException;


    /**
     *
     *
     * @param address
     * @return
     * @throws InterruptedException
     * @throws RemotingConnectException
     */
    Connector connect(UnresolvedAddress address) throws InterruptedException, RemotingConnectException;

    /**
     * 服务与 ChannelGroup
     *
     * @param directory
     * @param address
     * @return
     */
    boolean addChannelGroup(Directory directory, UnresolvedAddress address);

    /**
     *
     * @param directory
     * @param address
     * @return
     */
    boolean removeChannelGroup(Directory directory, UnresolvedAddress address);

    /**
     *
     * @param address
     * @return
     */
    ChannelGroup group(UnresolvedAddress address);

    /**
     *
     * @param directory
     * @return
     */
    List<ChannelGroup> directory(Directory directory);

    /**
     * 取消自动重连
     *
     * @param address
     */
    void cancelReconnect(UnresolvedAddress address);
}
