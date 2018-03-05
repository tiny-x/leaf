package com.leaf.rpc.consumer.dispatcher;

import com.leaf.common.ProtocolHead;
import com.leaf.common.model.RequestWrapper;
import com.leaf.remoting.api.channel.ChannelGroup;
import com.leaf.remoting.api.payload.RequestCommand;
import com.leaf.rpc.Request;
import com.leaf.rpc.balancer.LoadBalancer;
import com.leaf.rpc.consumer.Consumer;
import com.leaf.rpc.consumer.InvokeType;
import com.leaf.rpc.consumer.future.InvokeFuture;
import com.leaf.serialization.api.Serializer;
import com.leaf.serialization.api.SerializerType;

public class DefaultRoundDispatcher extends AbstractDispatcher {

    public DefaultRoundDispatcher(
            Consumer consumer, LoadBalancer loadBalancer, SerializerType serializerType) {
        super(consumer, loadBalancer, serializerType);
    }

    @Override
    public <T> InvokeFuture<T> dispatch(Request request, Class<T> returnType, InvokeType invokeType) throws Throwable {

        final RequestWrapper requestWrapper = request.getRequestWrapper();

        // 通过软负载均衡选择一个channel
        ChannelGroup channelGroup = select(requestWrapper.getServiceMeta());
        Serializer serializer = getSerializer();

        byte[] bytes = serializer.serialize(requestWrapper);
        RequestCommand requestCommand = new RequestCommand(ProtocolHead.REQUEST, getSerializerCode(), bytes);
        request.setRequestCommand(requestCommand);

        InvokeFuture<T> invoke = invoke(request, DispatchType.ROUND, returnType, invokeType, channelGroup);

        return invoke;
    }
}
