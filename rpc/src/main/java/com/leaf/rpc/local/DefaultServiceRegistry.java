package com.leaf.rpc.local;

import com.leaf.common.model.ServiceWrapper;
import com.leaf.rpc.container.ServiceProviderContainer;

public final class DefaultServiceRegistry implements ServiceRegistry {

    private Object serviceProvider;                     // 服务对象
    private Class<?> interfaceClass;                    // 接口类型
    private String group;                               // 服务组别
    private String providerName;                        // 服务名称
    private String version;                             // 服务版本号, 通常在接口不兼容时版本号才需要升级
    private int weight;                                 // 权重

    private ServiceProviderContainer serviceProviderContainer;

    public DefaultServiceRegistry(ServiceProviderContainer serviceProviderContainer) {
        this.serviceProviderContainer = serviceProviderContainer;
    }

    @Override
    public ServiceRegistry weight(int weight) {
        this.weight = weight;
        return this;
    }

    @Override
    public ServiceRegistry provider(Object serviceProvider) {
        this.serviceProvider = serviceProvider;
        return this;
    }

    @Override
    public ServiceRegistry interfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
        return this;
    }

    @Override
    public ServiceRegistry group(String group) {
        this.group = group;
        return this;
    }

    @Override
    public ServiceRegistry providerName(String providerName) {
        this.providerName = providerName;
        return this;
    }

    @Override
    public ServiceRegistry version(String version) {
        this.version = version;
        return this;
    }

    @Override
    public ServiceWrapper register() {
        ServiceWrapper wrapper = new ServiceWrapper(group, providerName, version, serviceProvider);

        serviceProviderContainer.registerService(wrapper.getServiceMeta().directory(), wrapper);

        return wrapper;
    }
}