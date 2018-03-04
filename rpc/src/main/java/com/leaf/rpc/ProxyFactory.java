package com.leaf.rpc;

import com.google.common.base.Strings;
import com.leaf.common.UnresolvedAddress;
import com.leaf.common.annotation.ServiceInterface;
import com.leaf.common.constants.Constants;
import com.leaf.common.model.Directory;
import com.leaf.common.model.RegisterMeta;
import com.leaf.common.model.ServiceMeta;
import com.leaf.common.utils.Proxies;
import com.leaf.register.api.NotifyEvent;
import com.leaf.register.api.NotifyListener;
import com.leaf.register.api.OfflineListener;
import com.leaf.rpc.balancer.RandomRobinLoadBalancer;
import com.leaf.rpc.consumer.Consumer;
import com.leaf.rpc.consumer.InvokeType;
import com.leaf.rpc.consumer.StrategyConfig;
import com.leaf.rpc.consumer.cluster.ClusterInvoker;
import com.leaf.rpc.consumer.dispatcher.DefaultBroadcastDispatcher;
import com.leaf.rpc.consumer.dispatcher.DefaultRoundDispatcher;
import com.leaf.rpc.consumer.dispatcher.DispatchType;
import com.leaf.rpc.consumer.dispatcher.Dispatcher;
import com.leaf.rpc.consumer.invoke.DefaultInvoker;
import com.leaf.serialization.api.SerializerType;
import io.netty.util.internal.SystemPropertyUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;


public class ProxyFactory {

    private static final SerializerType serializerType;

    static {
        serializerType = SerializerType.parse(
                (byte) SystemPropertyUtil.getInt("serializer.serializerType", SerializerType.PROTO_STUFF.value()));
    }

    private String group;

    private String serviceProviderName;

    private String version;

    private List<UnresolvedAddress> addresses;

    private Class<?> interfaceClass;

    private Consumer consumer;
    private long timeoutMillis;
    private InvokeType invokeType = InvokeType.SYNC;
    private ClusterInvoker.Strategy strategy = ClusterInvoker.Strategy.FAIL_FAST;
    private int retries = 0;
    private Dispatcher dispatcher;

    private ProxyFactory() {
    }

    public static ProxyFactory factory(Class<?> interfaceClass) {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.interfaceClass = interfaceClass;
        proxyFactory.addresses = new ArrayList<>();
        proxyFactory.serviceProviderName = interfaceClass.getName();
        return proxyFactory;
    }

    public ProxyFactory group(String group) {
        this.group = group;
        return this;
    }

    public ProxyFactory version(String version) {
        this.version = version;
        return this;
    }

    public ProxyFactory directory(Directory directory) {
        this.group(directory.getGroup())
                .version(directory.getVersion());
        return this;
    }

    public ProxyFactory consumer(Consumer consumer) {
        this.consumer = consumer;
        return this;
    }

    public ProxyFactory timeMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        return this;
    }

    public ProxyFactory invokeType(InvokeType invokeType) {
        this.invokeType = invokeType;
        return this;
    }

    public ProxyFactory strategy(ClusterInvoker.Strategy strategy) {
        this.strategy = strategy;
        return this;
    }


    public ProxyFactory retries(int retries) {
        this.retries = retries;
        return this;
    }

    public ProxyFactory providers(UnresolvedAddress... addresses) {
        Collections.addAll(this.addresses,  addresses);
        return this;
    }

    public ProxyFactory dispatcher(DispatchType dispatchType) {
        switch (dispatchType) {
            case BROADCAST: {
                dispatcher = new DefaultBroadcastDispatcher(
                        consumer,
                        RandomRobinLoadBalancer.instance(),
                        serializerType);
                break;
            }
            case ROUND: {
                dispatcher = new DefaultRoundDispatcher(
                        consumer,
                        RandomRobinLoadBalancer.instance(),
                        serializerType);
                break;
            }
        }
        return this;
    }


    @SuppressWarnings("unchecked")
    public <T> T newProxy() {

        checkNotNull(interfaceClass, "interfaceClass");
        ServiceInterface annotationInterface = interfaceClass.getAnnotation(ServiceInterface.class);
        if (annotationInterface != null) {
            checkArgument(
                    group == null,
                    interfaceClass.getName() + " has a @ServiceInterface annotation, can't set [group] again"
            );
            group = annotationInterface.group();
        }

        ServiceMeta serviceMeta = new ServiceMeta(group,
                serviceProviderName,
                Strings.isNullOrEmpty(version) ? Constants.SERVICE_VERSION : version);

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
                                    consumer.client().addChannelGroup(serviceMeta, registerMeta.getAddress());
                                }

                                consumer.client().group(registerMeta.getAddress())
                                        .setWeight(serviceMeta, registerMeta.getWeight());

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

        if (dispatcher == null) {
            dispatcher = new DefaultRoundDispatcher(
                    consumer,
                    RandomRobinLoadBalancer.instance(),
                    serializerType);
        }
        dispatcher.timeoutMillis(timeoutMillis);

        return (T) Proxies.getDefault().newProxy(
                interfaceClass,
                new DefaultInvoker(
                        consumer.application(),
                        dispatcher,
                        serviceMeta,
                        new StrategyConfig(strategy, retries),
                        invokeType
                ));
    }

}
