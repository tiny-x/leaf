package com.leaf.rpc.container;


import com.leaf.rpc.local.ServiceWrapper;

import java.util.List;

/**
 * Local service provider container.
 * <p>
 * 本地provider容器
 */
public interface ServiceProviderContainer {

    /**
     * 注册服务(注意并不是发布服务到注册中心, 只是注册到本地容器)
     */
    void registerService(String uniqueKey, ServiceWrapper serviceWrapper);

    /**
     * 本地容器查找服务
     */
    ServiceWrapper lookupService(String uniqueKey);

    /**
     * 从本地容器移除服务
     */
    ServiceWrapper removeService(String uniqueKey);

    /**
     * 获取本地容器中所有服务
     */
    List<ServiceWrapper> getAllServices();
}