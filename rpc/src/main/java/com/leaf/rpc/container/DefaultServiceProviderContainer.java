package com.leaf.rpc.container;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.leaf.rpc.local.ServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * 本地provider容器默认实现
 */
public final class DefaultServiceProviderContainer implements ServiceProviderContainer {

    private static final Logger logger = LoggerFactory.getLogger(DefaultServiceProviderContainer.class);

    private final ConcurrentMap<String, ServiceWrapper> serviceProviders = Maps.newConcurrentMap();

    @Override
    public void registerService(String uniqueKey, ServiceWrapper serviceWrapper) {
        serviceProviders.put(uniqueKey, serviceWrapper);

        logger.info("ServiceProvider [{}, {}] is registered.", uniqueKey, serviceWrapper);
    }

    @Override
    public ServiceWrapper lookupService(String uniqueKey) {
        return serviceProviders.get(uniqueKey);
    }

    @Override
    public ServiceWrapper removeService(String uniqueKey) {
        ServiceWrapper serviceWrapper = serviceProviders.remove(uniqueKey);
        if (serviceWrapper == null) {
            logger.warn("ServiceProvider [{}] not found.", uniqueKey);
        } else {
            logger.info("ServiceProvider [{}, {}] is removed.", uniqueKey, serviceWrapper);
        }
        return serviceWrapper;
    }

    @Override
    public List<ServiceWrapper> getAllServices() {
        return Lists.newArrayList(serviceProviders.values());
    }
}