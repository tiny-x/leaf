package com.leaf.rpc;

import com.leaf.common.UnresolvedAddress;
import com.leaf.common.annotation.ServiceInterface;
import com.leaf.common.constants.Constants;
import com.leaf.common.model.Directory;
import com.leaf.register.api.model.RegisterMeta;
import com.leaf.common.model.ServiceMeta;
import com.leaf.common.utils.Proxies;
import com.leaf.register.api.NotifyEvent;
import com.leaf.register.api.NotifyListener;
import com.leaf.register.api.OfflineListener;
import com.leaf.remoting.api.channel.ChannelGroup;
import com.leaf.rpc.balancer.LoadBalancerFactory;
import com.leaf.rpc.balancer.LoadBalancerType;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;


public class ProxyFactory {

    private SerializerType serializerType = SerializerType.PROTO_STUFF;

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
    private DispatchType dispatchType = DispatchType.ROUND;
    private LoadBalancerType loadBalancerType = LoadBalancerType.RANDOM;

    private ProxyFactory() {
    }

    public static ProxyFactory factory(Class<?> interfaceClass) {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.interfaceClass = interfaceClass;
        proxyFactory.addresses = new ArrayList<>();
        return proxyFactory;
    }

    public ProxyFactory group(String group) {
        this.group = group;
        return this;
    }

    public ProxyFactory serviceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
        return this;
    }

    public ProxyFactory version(String version) {
        this.version = version;
        return this;
    }

    public ProxyFactory directory(Directory directory) {
        this.group(directory.getGroup())
                .serviceProviderName(directory.getServiceProviderName())
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
        Collections.addAll(this.addresses, addresses);
        return this;
    }

    public ProxyFactory loadBalancerType(LoadBalancerType loadBalancerType) {
        this.loadBalancerType = loadBalancerType;
        return this;
    }

    public ProxyFactory dispatchType(DispatchType dispatchType) {
        this.dispatchType = dispatchType;
        return this;
    }

    public ProxyFactory serializerType(SerializerType serializerType) {
        this.serializerType = serializerType;
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

        ServiceMeta serviceMeta = new ServiceMeta(group == null ? Constants.DEFAULT_SERVICE_GROUP : group,
                serviceProviderName == null ? interfaceClass.getName() : serviceProviderName,
                version == null ? Constants.DEFAULT_SERVICE_VERSION : version);

        for (UnresolvedAddress address : addresses) {
            consumer.client().addChannelGroup(serviceMeta, address);
        }

        if (consumer.registerService() != null) {
            consumer.subscribe(serviceMeta, new NotifyListener() {
                @Override
                public void notify(RegisterMeta registerMeta, NotifyEvent event) {
                    ChannelGroup group = consumer.client().group(registerMeta.getAddress());
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
                                        consumer.client().cancelReconnect(registerMeta.getAddress());
                                        if (!group.isAvailable()) {
                                            consumer.client().removeChannelGroup(serviceMeta, registerMeta.getAddress());
                                        }
                                    }
                                });
                            }
                            break;
                        }
                        case REMOVE: {
                            consumer.client().removeChannelGroup(serviceMeta, registerMeta.getAddress());
                            group.removeWeight(serviceMeta);
                            break;
                        }
                    }
                }
            });
        }

        Dispatcher dispatcher;
        switch (dispatchType) {
            case ROUND:
                dispatcher = new DefaultRoundDispatcher(consumer, LoadBalancerFactory.instance(loadBalancerType), serializerType);
                break;
            case BROADCAST:
                dispatcher =  new DefaultBroadcastDispatcher(consumer, serializerType);
                break;
            default:
                throw new UnsupportedOperationException("dispatchType: " + dispatchType);
        }
        dispatcher.timeoutMillis(timeoutMillis <= 0 ? Constants.DEFAULT_INVOKE_TIMEOUT : timeoutMillis);

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
