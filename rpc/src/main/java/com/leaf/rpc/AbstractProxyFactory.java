package com.leaf.rpc;

import com.leaf.common.UnresolvedAddress;
import com.leaf.common.constants.Constants;
import com.leaf.common.model.Directory;
import com.leaf.common.model.ServiceMeta;
import com.leaf.register.api.NotifyEvent;
import com.leaf.register.api.NotifyListener;
import com.leaf.register.api.OfflineListener;
import com.leaf.register.api.model.RegisterMeta;
import com.leaf.remoting.api.channel.ChannelGroup;
import com.leaf.rpc.balancer.LoadBalancerFactory;
import com.leaf.rpc.balancer.LoadBalancerType;
import com.leaf.rpc.consumer.LeafClient;
import com.leaf.rpc.consumer.InvokeType;
import com.leaf.rpc.consumer.cluster.ClusterInvoker;
import com.leaf.rpc.consumer.dispatcher.DefaultBroadcastDispatcher;
import com.leaf.rpc.consumer.dispatcher.DefaultRoundDispatcher;
import com.leaf.rpc.consumer.dispatcher.DispatchType;
import com.leaf.rpc.consumer.dispatcher.Dispatcher;
import com.leaf.serialization.api.SerializerType;
import io.netty.util.internal.SystemPropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * @author yefei
 */
public abstract class AbstractProxyFactory {

    private static final Logger logger = LoggerFactory.getLogger(AbstractProxyFactory.class);

    protected static SerializerType defaultSerializerType;

    static {
        defaultSerializerType = SerializerType.parse(
                (byte) SystemPropertyUtil.getInt("serializer.serializerType", SerializerType.PROTO_STUFF.value()));
    }

    protected String group;

    protected String serviceProviderName;

    protected String version;

    protected SerializerType serializerType;

    protected List<UnresolvedAddress> addresses;

    protected LeafClient leafClient;

    protected long timeoutMillis;

    protected InvokeType invokeType = InvokeType.SYNC;
    protected ClusterInvoker.Strategy strategy = ClusterInvoker.Strategy.FAIL_FAST;
    protected int retries = 0;
    protected DispatchType dispatchType = DispatchType.ROUND;
    protected LoadBalancerType loadBalancerType = LoadBalancerType.RANDOM;

    public AbstractProxyFactory() {
        this.serializerType = defaultSerializerType;
    }

    public AbstractProxyFactory group(String group) {
        this.group = group;
        return this;
    }

    public AbstractProxyFactory serviceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
        return this;
    }

    public AbstractProxyFactory version(String version) {
        this.version = version;
        return this;
    }

    public AbstractProxyFactory directory(Directory directory) {
        this.group(directory.getGroup())
                .serviceProviderName(directory.getServiceProviderName())
                .version(directory.getVersion());
        return this;
    }

    public AbstractProxyFactory consumer(LeafClient leafClient) {
        this.leafClient = leafClient;
        return this;
    }

    public AbstractProxyFactory timeMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        return this;
    }

    public AbstractProxyFactory invokeType(InvokeType invokeType) {
        this.invokeType = invokeType;
        return this;
    }

    public AbstractProxyFactory strategy(ClusterInvoker.Strategy strategy) {
        this.strategy = strategy;
        return this;
    }

    public AbstractProxyFactory retries(int retries) {
        this.retries = retries;
        return this;
    }

    public AbstractProxyFactory providers(UnresolvedAddress... addresses) {
        Collections.addAll(this.addresses, addresses);
        return this;
    }

    public AbstractProxyFactory loadBalancerType(LoadBalancerType loadBalancerType) {
        this.loadBalancerType = loadBalancerType;
        return this;
    }

    public AbstractProxyFactory dispatchType(DispatchType dispatchType) {
        this.dispatchType = dispatchType;
        return this;
    }

    public AbstractProxyFactory serializerType(SerializerType serializerType) {
        this.serializerType = serializerType;
        return this;
    }

    protected void subscribe(ServiceMeta serviceMeta) {
        leafClient.subscribe(serviceMeta, new NotifyListener<RegisterMeta>() {
            @Override
            public void notify(RegisterMeta registerMeta, NotifyEvent event) {
                ChannelGroup group = leafClient.remotingClient().group(registerMeta.getAddress());
                switch (event) {
                    case ADD: {
                        if (!leafClient.remotingClient().group(registerMeta.getAddress()).isAvailable()) {
                            int connCount = registerMeta.getConnCount() < 1 ? 1 : registerMeta.getConnCount();
                            for (int i = 0; i < connCount; i++) {
                                leafClient.connect(registerMeta.getAddress());
                            }
                            leafClient.offlineListening(registerMeta.getAddress(), new OfflineListener() {
                                @Override
                                public void offline() {
                                    leafClient.remotingClient().cancelReconnect(registerMeta.getAddress());
                                    if (!group.isAvailable()) {
                                        leafClient.remotingClient().removeChannelGroup(serviceMeta, registerMeta.getAddress());
                                    }
                                }
                            });
                        }
                        // channelGroup 和 serviceMeta 关系
                        leafClient.remotingClient().addChannelGroup(serviceMeta, registerMeta.getAddress());
                        // 设置channelGroup(相同地址的channel) weight
                        leafClient.remotingClient()
                                .group(registerMeta.getAddress())
                                .setWeight(serviceMeta, registerMeta.getWeight());
                        break;
                    }
                    case REMOVE: {
                        leafClient.remotingClient().removeChannelGroup(serviceMeta, registerMeta.getAddress());
                        group.removeWeight(serviceMeta);
                        break;
                    }
                    default:
                }
            }
        });
    }

    public abstract <T> T newProxy();

    protected Dispatcher dispatcher(DispatchType dispatchType, LeafClient leafClient, LoadBalancerType loadBalancerType, long timeoutMillis) {
        Dispatcher dispatcher;
        switch (dispatchType) {
            case ROUND:
                dispatcher = new DefaultRoundDispatcher(leafClient, LoadBalancerFactory.instance(loadBalancerType), serializerType);
                break;
            case BROADCAST:
                dispatcher = new DefaultBroadcastDispatcher(leafClient, serializerType);
                break;
            default:
                throw new UnsupportedOperationException("dispatchType: " + dispatchType);
        }
        dispatcher.timeoutMillis(timeoutMillis <= 0 ? Constants.DEFAULT_INVOKE_TIMEOUT : timeoutMillis);
        return dispatcher;
    }
}
