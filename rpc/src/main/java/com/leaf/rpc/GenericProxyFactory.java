package com.leaf.rpc;

import com.google.common.base.Strings;
import com.leaf.common.UnresolvedAddress;
import com.leaf.common.constants.Constants;
import com.leaf.common.model.ServiceMeta;
import com.leaf.rpc.consumer.StrategyConfig;
import com.leaf.rpc.consumer.dispatcher.Dispatcher;
import com.leaf.rpc.consumer.invoke.GenericInvoke;

import java.util.ArrayList;

import static com.google.common.base.Preconditions.checkNotNull;

public class GenericProxyFactory extends AbstractProxyFactory {

    public static GenericProxyFactory factory() {
        GenericProxyFactory proxyFactory = new GenericProxyFactory();
        proxyFactory.addresses = new ArrayList<>();
        return proxyFactory;
    }

    public GenericInvoke newProxy() {
        checkNotNull(group, "interfaceClass");
        checkNotNull(serviceProviderName, "serviceProviderName");

        ServiceMeta serviceMeta = new ServiceMeta(
                Strings.isNullOrEmpty(group) ? Constants.DEFAULT_SERVICE_GROUP : group,
                serviceProviderName,
                Strings.isNullOrEmpty(version) ? Constants.DEFAULT_SERVICE_VERSION : version);

        for (UnresolvedAddress address : addresses) {
            consumer.client().addChannelGroup(serviceMeta, address);
        }

        if (consumer.registerService() != null) {
            subscribe(serviceMeta);
        }

        Dispatcher dispatcher = dispatcher(dispatchType, consumer, loadBalancerType, timeoutMillis);

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
