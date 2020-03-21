package com.leaf.rpc.local;

import com.google.common.base.Strings;
import com.leaf.common.annotation.ServiceInterface;
import com.leaf.common.annotation.ServiceProvider;
import com.leaf.common.constants.Constants;
import com.leaf.rpc.container.ServiceProviderContainer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

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

        checkNotNull(serviceProvider, "serviceProvider is null");

        ServiceProvider annotationProvider = this.serviceProvider.getClass().getAnnotation(ServiceProvider.class);
        ServiceInterface annotationInterface = null;

        for (Class<?> aClass : this.serviceProvider.getClass().getInterfaces()) {
            annotationInterface = aClass.getAnnotation(ServiceInterface.class);
            if (annotationInterface != null) {
                checkArgument(
                        interfaceClass == null,
                        serviceProvider.getClass().getName()
                                + " has a @ServiceProvider annotation, can't set [interfaceClass] again"
                );
                interfaceClass = aClass;
                break;
            }
        }

        checkNotNull(interfaceClass, "interfaceClass is null");

        if (annotationProvider != null) {
            checkArgument(
                    version == null,
                    serviceProvider.getClass().getName()
                            + " has a @ServiceProvider annotation, can't set [version] again"
            );

            checkArgument(
                    weight == 0,
                    serviceProvider.getClass().getName()
                            + " has a @ServiceProvider annotation, can't set [weight] again"
            );

            version = annotationProvider.version();
            weight = annotationProvider.weight();
        }

        if (annotationInterface != null) {
            checkArgument(
                    group == null,
                    interfaceClass.getName() + " has a @ServiceInterface annotation, can't set [group] again"
            );
            group = annotationInterface.group();
        }

        ServiceWrapper wrapper = new ServiceWrapper(
                Strings.isNullOrEmpty(group) ? Constants.DEFAULT_SERVICE_GROUP : group,
                Strings.isNullOrEmpty(providerName) ? interfaceClass.getName() : providerName,
                Strings.isNullOrEmpty(version) ? Constants.DEFAULT_SERVICE_VERSION : version,
                serviceProvider,
                weight);

        serviceProviderContainer.registerService(wrapper.getServiceMeta().directory(), wrapper);

        return wrapper;
    }
}