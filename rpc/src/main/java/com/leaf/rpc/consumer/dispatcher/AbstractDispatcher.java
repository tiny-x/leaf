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
import com.leaf.rpc.consumer.future.RpcFutureGroup;
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
        CopyOnWriteArrayList<ChannelGroup> channelGroups = consumer.client().directory(metadata);
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

    protected Object invoke(final Request request,
                            final DispatchType dispatchType,
                            InvokeType invokeType,
                            ChannelGroup... channelGroup) throws RemotingException, InterruptedException {

        switch (invokeType) {
            case SYNC: {
                if (dispatchType == DispatchType.BROADCAST) {
                    throw new UnsupportedOperationException("syncInvoke Unsupported broadcast dispatch!");
                }
                return invokeSync(request, channelGroup[0]);
            }
            case ASYNC: {
                invokeAsync(request, dispatchType, channelGroup);
                return null;
            }
            case ONE_WAY: {
                invokeOneWay(request, dispatchType, channelGroup);
                return null;
            }
            default: {
                String errorMessage = String.format("Unsupported InvokeType: %s", invokeType.name());
                throw new UnsupportedOperationException(errorMessage);
            }
        }

    }

    private Object invokeSync(Request request, ChannelGroup channelGroup) throws RemotingException, InterruptedException {
        ResponseCommand responseCommand = consumer
                .client()
                .invokeSync(channelGroup.remoteAddress(),
                        request.getRequestCommand(),
                        timeoutMillis);

        ResponseWrapper responseWrapper = getSerializer().deserialize(responseCommand.getBody(), ResponseWrapper.class);
        if (responseCommand.getStatus() == ResponseStatus.SUCCESS.value()) {
            return responseWrapper.getResult();
        } else {
            logger.warn("[INVOKE FAIL] directory: {}, method: {}, message: {}",
                    request.getRequestWrapper().getServiceMeta().directory(),
                    request.getRequestWrapper().getMethodName(),
                    responseWrapper.getResult());
            return null;
        }
    }

    private void invokeAsync(Request request, DispatchType dispatchType, ChannelGroup... channelGroup) throws RemotingException, InterruptedException {
        switch (dispatchType) {
            case ROUND: {
                RpcFuture future = new RpcFuture();
                RpcContext.setFuture(future);
                consumer.client().invokeAsync(
                        channelGroup[0].remoteAddress(),
                        request.getRequestCommand(),
                        timeoutMillis,
                        new InvokeAsyncCallback(future));
                break;
            }
            case BROADCAST: {
                RpcFuture[] futures = new RpcFuture[channelGroup.length];
                RpcFutureGroup rpcFutureGroup = new RpcFutureGroup(futures);
                for (int i = 0; i < channelGroup.length; i++) {
                    futures[i] = new RpcFuture();
                    request.getRequestCommand().getAndIncrement();
                    consumer.client().invokeAsync(
                            channelGroup[i].remoteAddress(),
                            request.getRequestCommand(),
                            timeoutMillis,
                            new InvokeAsyncCallback(futures[i]));
                }
                RpcContext.setRpcFutureGroup(rpcFutureGroup);
                break;
            }
            default: {
                String errorMessage = String.format("Unsupported DispatchType: %s",
                        dispatchType.name());
                throw new UnsupportedOperationException(errorMessage);
            }
        }

    }

    private void invokeOneWay(Request request, DispatchType dispatchType, ChannelGroup... channelGroup) throws RemotingException, InterruptedException {
        switch (dispatchType) {
            case ROUND: {
                consumer.client().invokeOneWay(channelGroup[0].remoteAddress(),
                        request.getRequestCommand(),
                        timeoutMillis);
                break;
            }
            case BROADCAST: {
                for (ChannelGroup group : channelGroup) {
                    request.getRequestCommand().getAndIncrement();
                    consumer.client().invokeOneWay(group.remoteAddress(),
                            request.getRequestCommand(),
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

    class InvokeAsyncCallback implements InvokeCallback<ResponseCommand> {

        private RpcFuture future;

        public InvokeAsyncCallback(RpcFuture future) {
            this.future = future;
        }

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
    }
}
