package com.leaf.rpc.local;

import com.leaf.common.model.ServiceWrapper;

/**
 * 本地服务注册.
 */
public interface ServiceRegistry {

    /**
     * 设置服务权重(0 < weight <= 100).
     */
    ServiceRegistry weight(int weight);

    ServiceRegistry provider(Object serviceProvider);

    ServiceRegistry interfaceClass(Class<?> interfaceClass);

    ServiceRegistry group(String group);

    ServiceRegistry providerName(String providerName);

    ServiceRegistry version(String version);

    /**
     * 注册服务到本地容器.
     */
    ServiceWrapper register();
}