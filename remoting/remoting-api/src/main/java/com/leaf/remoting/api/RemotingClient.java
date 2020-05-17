package com.leaf.remoting.api;

import com.leaf.remoting.api.channel.ChannelGroup;
import com.leaf.remoting.api.payload.ResponseCommand;
import com.leaf.common.UnresolvedAddress;
import com.leaf.common.model.Directory;
import com.leaf.remoting.api.payload.RequestCommand;
import com.leaf.remoting.api.exception.RemotingConnectException;
import com.leaf.remoting.api.exception.RemotingException;
import io.netty.channel.Channel;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

public interface RemotingClient extends RemotingService {

    Connector connect(UnresolvedAddress address)
            throws InterruptedException, RemotingConnectException;

    boolean addChannelGroup(Directory directory, UnresolvedAddress address);

    boolean removeChannelGroup(Directory directory, UnresolvedAddress address);

    ChannelGroup group(UnresolvedAddress address);

    boolean hasAvailableChannelGroup(UnresolvedAddress address);

    CopyOnWriteArrayList<ChannelGroup> directory(Directory directory);

    boolean isDirectoryAvailable(Directory directory);

    ResponseCommand invokeSync(final Channel channel, final RequestCommand request, long timeoutMillis)
            throws RemotingException, InterruptedException;

    ResponseCommand invokeSync(final UnresolvedAddress address, final RequestCommand request, long timeoutMillis)
            throws RemotingException, InterruptedException;

    void invokeAsync(final Channel channel, final RequestCommand request
            , long timeoutMillis, InvokeCallback<ResponseCommand> invokeCallback)
            throws RemotingException, InterruptedException;

    void invokeAsync(final UnresolvedAddress address, final RequestCommand request
            , long timeoutMillis, InvokeCallback<ResponseCommand> invokeCallback)
            throws RemotingException, InterruptedException;

    void invokeOneWay(final Channel channel, final RequestCommand request, long timeoutMillis)
            throws RemotingException, InterruptedException;

    void invokeOneWay(final UnresolvedAddress address, final RequestCommand request, long timeoutMillis)
            throws RemotingException, InterruptedException;

    void registerRequestProcess(RequestCommandProcessor requestCommandProcessor, ExecutorService executor);

    void cancelReconnect(UnresolvedAddress address);
}
