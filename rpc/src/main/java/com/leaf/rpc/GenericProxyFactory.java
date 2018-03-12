package com.leaf.rpc;

import com.google.common.base.Strings;
import com.leaf.common.UnresolvedAddress;
import com.leaf.common.constants.Constants;
import com.leaf.common.model.Directory;
import com.leaf.register.api.model.RegisterMeta;
import com.leaf.common.model.ServiceMeta;
import com.leaf.register.api.NotifyEvent;
import com.leaf.register.api.NotifyListener;
import com.leaf.register.api.OfflineListener;
import com.leaf.rpc.balancer.RoundRobinLoadBalancer;
import com.leaf.rpc.consumer.Consumer;
import com.leaf.rpc.consumer.InvokeType;
import com.leaf.rpc.consumer.StrategyConfig;
import com.leaf.rpc.consumer.cluster.ClusterInvoker;
import com.leaf.rpc.consumer.dispatcher.DefaultRoundDispatcher;
import com.leaf.rpc.consumer.dispatcher.Dispatcher;
import com.leaf.rpc.consumer.invoke.GenericInvoke;
import com.leaf.serialization.api.SerializerType;
import io.netty.util.internal.SystemPropertyUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class GenericProxyFactory {

    private static final SerializerType serializerType;

    static {
        serializerType = SerializerType.parse(
                (byte) SystemPropertyUtil.getInt("serializer.serializerType", SerializerType.PROTO_STUFF.value()));
    }

    private String group;

    private String serviceProviderName;

    private String version;

    private List<UnresolvedAddress> addresses;

    private Consumer consumer;
    private long timeoutMillis;
    private InvokeType invokeType = InvokeType.SYNC;
    private ClusterInvoker.Strategy strategy = ClusterInvoker.Strategy.FAIL_FAST;
    private int retries = 0;

    public static GenericProxyFactory factory() {
        GenericProxyFactory proxyFactory = new GenericProxyFactory();
        proxyFactory.addresses = new ArrayList<>();
        return proxyFactory;
    }

    public GenericProxyFactory group(String group) {
        this.group = group;
        return this;
    }

    public GenericProxyFactory serviceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
        return this;
    }

    public GenericProxyFactory version(String version) {
        this.version = version;
        return this;
    }

    public GenericProxyFactory directory(Directory directory) {
        this.group(directory.getGroup())
                .serviceProviderName(directory.getServiceProviderName())
                .version(directory.getVersion());
        return this;
    }

    public GenericProxyFactory providers(UnresolvedAddress... addresses) {
        Collections.addAll(this.addresses, addresses);
        return this;
    }

    public GenericProxyFactory consumer(Consumer consumer) {
        this.consumer = consumer;
        return this;
    }

    public GenericProxyFactory timeMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        return this;
    }

    public GenericProxyFactory invokeType(InvokeType invokeType) {
        this.invokeType = invokeType;
        return this;
    }

    public GenericProxyFactory strategy(ClusterInvoker.Strategy strategy) {
        this.strategy = strategy;
        return this;
    }

    public GenericProxyFactory retries(int retries) {
        this.retries = retries;
        return this;
    }

    @SuppressWarnings("unchecked")
    public GenericInvoke newProxy() {
        checkNotNull(group, "interfaceClass");
        checkNotNull(serviceProviderName, "serviceProviderName");

        ServiceMeta serviceMeta = new ServiceMeta(
                group,
                serviceProviderName,
                Strings.isNullOrEmpty(version) ? Constants.DEFAULT_SERVICE_VERSION : version);

        for (UnresolvedAddress address : addresses) {
            consumer.client().addChannelGroup(serviceMeta, address);
        }

        if (consumer.registerService() != null) {
            consumer.subscribe(serviceMeta, new NotifyListener() {
                @Override
                public void notify(RegisterMeta registerMeta, NotifyEvent event) {
                    switch (event) {
                        case ADD: {
                            if (!consumer.client().hasAvailableChannelGroup(registerMeta.getAddress())) {
                                int connCount = registerMeta.getConnCount() < 1 ? 1 : registerMeta.getConnCount();
                                for (int i = 0; i < connCount; i++) {
                                    consumer.connect(registerMeta.getAddress());
                                }

                                // 设置channelGroup(相同地址的channel) weight
                                consumer.client()
                                        .group(registerMeta.getAddress())
                                        .setWeight(serviceMeta, registerMeta.getWeight());

                                // channelGroup 和 serviceMeta 关系
                                consumer.client().addChannelGroup(serviceMeta, registerMeta.getAddress());

                                consumer.offlineListening(registerMeta.getAddress(), new OfflineListener() {
                                    @Override
                                    public void offline() {
                                        consumer.client().removeChannelGroup(serviceMeta, registerMeta.getAddress());
                                    }
                                });
                            }
                            break;
                        }
                        case REMOVE: {
                            consumer.client().removeChannelGroup(serviceMeta, registerMeta.getAddress());
                            break;
                        }
                    }
                }
            });
        }

        Dispatcher dispatcher = new DefaultRoundDispatcher(
                consumer,
                RoundRobinLoadBalancer.instance(),
                serializerType);

        dispatcher.timeoutMillis(timeoutMillis);

        GenericInvoke genericInvoke = new GenericInvoke(
                consumer.application(),
                dispatcher,
                serviceMeta,
                new StrategyConfig(strategy, retries),
                invokeType
        );
        return genericInvoke;
    }

}
