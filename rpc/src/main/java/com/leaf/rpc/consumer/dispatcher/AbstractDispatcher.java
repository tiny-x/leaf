package com.leaf.rpc.consumer.dispatcher;

import com.leaf.common.context.RpcContext;
import com.leaf.remoting.api.ResponseWrapper;
import com.leaf.common.model.ServiceMeta;
import com.leaf.remoting.api.InvokeCallback;
import com.leaf.remoting.api.ResponseStatus;
import com.leaf.remoting.api.channel.ChannelGroup;
import com.leaf.remoting.api.exception.RemotingException;
import com.leaf.remoting.api.future.ResponseFuture;
import com.leaf.remoting.api.payload.RequestCommand;
import com.leaf.remoting.api.payload.ResponseCommand;
import com.leaf.rpc.balancer.LoadBalancer;
import com.leaf.rpc.consumer.Consumer;
import com.leaf.rpc.consumer.InvokeType;
import com.leaf.rpc.consumer.future.DefaultInvokeFuture;
import com.leaf.rpc.consumer.future.DefaultInvokeFutureGroup;
import com.leaf.rpc.consumer.future.InvokeFuture;
import com.leaf.serialization.api.Serializer;
import com.leaf.serialization.api.SerializerFactory;
import com.leaf.serialization.api.SerializerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CopyOnWriteArrayList;

import static com.google.common.base.Preconditions.checkState;

public abstract class AbstractDispatcher implements Dispatcher {

    private final static Logger logger = LoggerFactory.getLogger(AbstractDispatcher.class);

    protected long timeoutMillis;
    private Consumer consumer;
    private LoadBalancer loadBalancer;
    private SerializerType serializerType;

    public AbstractDispatcher(Consumer consumer, SerializerType serializerType) {
        this(consumer, null, serializerType);
    }

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
        throw new IllegalStateException(metadata + " no channel");
    }

    protected ChannelGroup[] groups(ServiceMeta metadata) {
        CopyOnWriteArrayList<ChannelGroup> channelGroups = consumer.client().directory(metadata);
        checkState(channelGroups.size() > 0, metadata + " no channel");

        ChannelGroup[] channelGroupsArray = new ChannelGroup[channelGroups.size()];
        channelGroups.toArray(channelGroupsArray);
        return channelGroupsArray;
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

    protected <T> InvokeFuture<T> invoke(final RequestCommand requestCommand,
                                         final DispatchType dispatchType,
                                         Class<T> returnType,
                                         InvokeType invokeType,
                                         ChannelGroup... channelGroup) throws Throwable {
        InvokeFuture<T> invokeFuture = null;
        switch (invokeType) {
            case SYNC: {
                if (dispatchType == DispatchType.BROADCAST) {
                    throw new UnsupportedOperationException("syncInvoke Unsupported broadcast dispatch!");
                }
                invokeFuture = invokeSync(requestCommand, returnType, channelGroup[0]);
                break;
            }
            case ASYNC: {
                invokeFuture = invokeAsync(requestCommand, dispatchType, returnType, channelGroup);
                break;
            }
            case ONE_WAY: {
                invokeOneWay(requestCommand, dispatchType, channelGroup);
                break;
            }
            default: {
                String errorMessage = String.format("Unsupported InvokeType: %s", invokeType.name());
                throw new UnsupportedOperationException(errorMessage);
            }
        }
        RpcContext.clearAttachments();
        return invokeFuture;
    }

    private <T> InvokeFuture<T> invokeSync(RequestCommand requestCommand, Class<T> returnType, ChannelGroup channelGroup) throws Throwable {
        InvokeFuture<T> invokeFuture = new DefaultInvokeFuture<>(returnType, timeoutMillis);
        ResponseCommand responseCommand = consumer
                .client()
                .invokeSync(channelGroup.remoteAddress(),
                        requestCommand,
                        timeoutMillis);

        ResponseWrapper responseWrapper = getSerializer().deserialize(responseCommand.getBody(), ResponseWrapper.class);
        if (responseCommand.getStatus() == ResponseStatus.SUCCESS.value()) {
            invokeFuture.complete((T) responseWrapper.getResult());
        } else {
            Throwable throwable = handlerException(responseCommand);
            invokeFuture.complete((T) throwable);
        }
        return invokeFuture;
    }

    private <T> InvokeFuture<T> invokeAsync(RequestCommand requestCommand, DispatchType dispatchType, Class<T> returnType, ChannelGroup... channelGroup) throws Throwable {
        InvokeFuture<T> invokeFuture = null;
        switch (dispatchType) {
            case ROUND: {
                invokeFuture = new DefaultInvokeFuture<T>(returnType, timeoutMillis);
                consumer.client().invokeAsync(
                        channelGroup[0].remoteAddress(),
                        requestCommand,
                        timeoutMillis,
                        new InvokeAsyncCallback(invokeFuture));
                return invokeFuture;
            }
            case BROADCAST: {
                DefaultInvokeFuture[] futures = new DefaultInvokeFuture[channelGroup.length];
                invokeFuture = new DefaultInvokeFutureGroup(futures);
                for (int i = 0; i < channelGroup.length; i++) {
                    futures[i] = new DefaultInvokeFuture<T>(returnType, timeoutMillis);
                    consumer.client().invokeAsync(
                            channelGroup[i].remoteAddress(),
                            requestCommand.clone(),
                            timeoutMillis,
                            new InvokeAsyncCallback(futures[i]));
                }
                return invokeFuture;
            }
            default: {
                String errorMessage = String.format("Unsupported DispatchType: %s",
                        dispatchType.name());
                throw new UnsupportedOperationException(errorMessage);
            }
        }

    }

    private void invokeOneWay(RequestCommand requestCommand, DispatchType dispatchType, ChannelGroup... channelGroup) throws Throwable {
        switch (dispatchType) {
            case ROUND: {
                consumer.client().invokeOneWay(channelGroup[0].remoteAddress(),
                        requestCommand,
                        timeoutMillis);
                break;
            }
            case BROADCAST: {
                for (int i = 0; i < channelGroup.length; i++) {
                    consumer.client().invokeOneWay(channelGroup[i].remoteAddress(),
                            requestCommand.clone(),
                            timeoutMillis);
                }
            }
            default: {
                String errorMessage = String.format("Unsupported DispatchType: %s",
                        dispatchType.name());
                throw new UnsupportedOperationException(errorMessage);
            }
        }

    }

    // 服务端异常 未找到服务，service抛的异常等
    private Throwable handlerException(ResponseCommand responseCommand) {
        Throwable cause;
        ResponseWrapper responseWrapper = getSerializer().deserialize(responseCommand.getBody(), ResponseWrapper.class);

        if (responseCommand.getStatus() == ResponseStatus.SERVER_ERROR.value()) {
            cause = (Throwable) responseWrapper.getResult();
        } else {
            cause = new RemotingException(responseWrapper.getResult().toString());
        }
        return cause;
    }

    class InvokeAsyncCallback implements InvokeCallback<ResponseCommand> {

        private InvokeFuture<Object> future;

        public <T> InvokeAsyncCallback(InvokeFuture<T> future) {
            this.future = (InvokeFuture<Object>) future;
        }

        @Override
        public void operationComplete(ResponseFuture<ResponseCommand> responseFuture) {
            ResponseCommand responseCommand = responseFuture.result();

            if (responseCommand != null) {
                if (responseCommand.getStatus() == ResponseStatus.SUCCESS.value()) {
                    ResponseWrapper responseWrapper = getSerializer().deserialize(responseCommand.getBody(), ResponseWrapper.class);
                    future.complete(responseWrapper.getResult());
                    future.notifyListener(responseWrapper.getResult());
                } else {
                    Throwable cause = handlerException(responseCommand);
                    future.complete(cause);
                    future.notifyListener(cause);
                }
            } else {
                // 通常是客户端异常 或 等待服务端超时
                Throwable cause = responseFuture.cause();
                if (cause != null) {
                    future.complete(cause);
                    future.notifyListener(cause);
                } else {
                    logger.warn("Not only not received any message from provider, but cause is null!");
                }
            }
        }
    }
}
