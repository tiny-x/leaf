package com.leaf.spring.init.bean;

import com.leaf.common.model.ServiceWrapper;
import com.leaf.rpc.local.ServiceRegistry;
import com.leaf.rpc.provider.Provider;
import org.springframework.beans.factory.InitializingBean;

import static com.google.common.base.Preconditions.checkNotNull;

public class SpringServiceBean implements InitializingBean {

    private SpringProvider provider;

    private int weight;

    //----------------------
    private String group;

    private String serviceProviderName;

    private String version;
    // -----------------------

    private Class<?> interfaceClass;

    private Object ref;

    @Override
    public void afterPropertiesSet() throws Exception {
        checkNotNull(provider, "provider");
        Provider _provider = provider.getProvider();

        ServiceRegistry serviceRegistry = _provider.serviceRegistry()
                .provider(ref)
                .group(group)
                .interfaceClass(interfaceClass)
                .version(this.version);
        if (serviceProviderName != null) {
            serviceRegistry.providerName(serviceProviderName);
        }
        if (weight > 0) {
            serviceRegistry.weight(weight);
        }

        ServiceWrapper serviceWrapper = serviceRegistry
                .register();
        _provider.publishService(serviceWrapper);
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setInterfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setRef(Object ref) {
        this.ref = ref;
    }

    public void setProvider(SpringProvider provider) {
        this.provider = provider;
    }

    public void setServiceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
