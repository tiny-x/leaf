package com.leaf.spring.init.bean;

import com.leaf.common.model.ServiceWrapper;
import com.leaf.rpc.provider.Provider;
import org.springframework.beans.factory.InitializingBean;

import static com.google.common.base.Preconditions.checkNotNull;

public class SpringServiceBean implements InitializingBean {

    private SpringProvider provider;

    private String group;

    private Class<?> interfaceClass;

    private String version;

    private Object ref;

    @Override
    public void afterPropertiesSet() throws Exception {
        checkNotNull(provider, "provider");
        Provider _provider = provider.getProvider();

        ServiceWrapper serviceWrapper = _provider.serviceRegistry()
                .provider(ref)
                .group(group)
                .interfaceClass(interfaceClass)
                .version(version)
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
}
