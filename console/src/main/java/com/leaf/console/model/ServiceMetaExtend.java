package com.leaf.console.model;

import com.leaf.common.model.ServiceMeta;

public class ServiceMetaExtend extends ServiceMeta {

    private Integer providers;

    private Integer consumers;

    public ServiceMetaExtend(String group, String serviceProviderName, String version) {
        super(group, serviceProviderName, version);
    }

    public Integer getProviders() {
        return providers;
    }

    public void setProviders(Integer providers) {
        this.providers = providers;
    }

    public Integer getConsumers() {
        return consumers;
    }

    public void setConsumers(Integer consumers) {
        this.consumers = consumers;
    }
}
