package com.leaf.rpc.consumer.dispatcher;


import com.leaf.common.model.ResponseWrapper;
import com.leaf.common.model.ServiceMeta;
import com.leaf.remoting.api.InvokeCallback;
import com.leaf.remoting.api.ResponseStatus;
import com.leaf.remoting.api.channel.ChannelGroup;
import com.leaf.remoting.api.future.ResponseFuture;
import com.leaf.remoting.api.payload.ResponseCommand;
import com.leaf.remoting.exception.RemotingException;
import com.leaf.rpc.Request;
import com.leaf.rpc.balancer.LoadBalancer;
import com.leaf.rpc.consumer.Consumer;
import com.leaf.rpc.consumer.InvokeType;
import com.leaf.rpc.consumer.future.RpcContext;
import com.leaf.rpc.consumer.future.RpcFuture;
import com.leaf.rpc.consumer.future.RpcFutureListener;
import com.leaf.serialization.api.Serializer;
import com.leaf.serialization.api.SerializerFactory;
import com.leaf.serialization.api.SerializerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractDispatcher implements Dispatcher {

    private static final Logger logger = LoggerFactory.getLogger(AbstractDispatcher.class);
    protected long timeoutMillis;
    private Consumer consumer;
    private LoadBalancer loadBalancer;
    private SerializerType serializerType;

    public AbstractDispatcher(Consumer consumer, LoadBalancer loadBalancer, SerializerType serializerType) {
        this.consumer = consumer;
        this.loadBalancer = loadBalancer;
        this.serializerType = serializerType;
    }

    protected ChannelGroup select(ServiceMeta metadata) {
        CopyOnWriteArrayList<ChannelGroup> groups = consumer.client().directory(metadata);

        ChannelGroup group = loadBalancer.select(groups, metadata);

        if (group != null) {
            if (group.isAvailable()) {
                return group;
            }
        }

        for (ChannelGroup g : groups) {
            if (g.isAvailable()) {
                return g;
            }
        }
        throw new IllegalStateException("no channel");
    }

    protected ChannelGroup[] groups(ServiceMeta metadata) {
        return (ChannelGroup[]) consumer.client().directory(metadata).toArray();
    }

    @Override
    public Dispatcher timeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        return this;
    }

    protected Serializer getSerializer() {
        return SerializerFactory.serializer(serializerType);
    }

    protected byte getSerializerCode() {
        return serializerType.value();
    }

    protected Object invoke(ChannelGroup channelGroup,
                            final Request request,
                            final DispatchType dispatchType,
                            InvokeType invokeType) throws RemotingException, InterruptedException {
        switch (invokeType) {
            case SYNC: {
                ResponseCommand responseCommand = consumer
                        .client()
                        .invokeSync(channelGroup.remoteAddress(),
                                request.getRequestCommand(),
                                timeoutMillis);

                if (responseCommand.getStatus() == ResponseStatus.SUCCESS.value()) {
                    ResponseWrapper responseWrapper = getSerializer().deserialize(responseCommand.getBody(), ResponseWrapper.class);
                    return responseWrapper.getResult();

                } else {
                    ResponseWrapper responseWrapper = getSerializer().deserialize(responseCommand.getBody(), ResponseWrapper.class);
                    logger.warn("[INVOKE FAIL] directory: {}, method: {}, message: {}",
                            request.getRequestWrapper().getServiceMeta().directory(),
                            request.getRequestWrapper().getMethodName(),
                            responseWrapper.getResult());
                    return null;
                }
            }
            case ASYNC: {
                RpcFuture future = new RpcFuture();
                RpcContext.setFuture(future);
                consumer.client().invokeAsync(
                        channelGroup.remoteAddress(),
                        request.getRequestCommand(),
                        timeoutMillis,
                        new InvokeCallback<ResponseCommand>() {
                            @Override
                            public void operationComplete(ResponseFuture<ResponseCommand> responseFuture) {
                                RpcFutureListener listener = future.getListener();

                                if (responseFuture.isSuccess()) {
                                    ResponseCommand responseCommand = responseFuture.result();
                                    ResponseWrapper responseWrapper = getSerializer().deserialize(responseCommand.getBody(), ResponseWrapper.class);
                                    future.complete(responseWrapper.getResult());
                                    if (listener != null) {
                                        listener.complete(responseWrapper.getResult());
                                    }
                                } else {
                                    future.complete(null);
                                    if (listener != null) {
                                        listener.failure(responseFuture.cause());
                                    }
                                }
                            }
                        });

                return null;
            }
            case ONE_WAY: {
                consumer.client().invokeOneWay(channelGroup.remoteAddress(),
                                request.getRequestCommand(),
                                timeoutMillis);
            }
            default: {
                String errorMessage = String.format("DefaultProviderProcessor Unsupported InvokeType: %d",
                        invokeType.name());
                throw new UnsupportedOperationException(errorMessage);
            }
        }

    }
}
