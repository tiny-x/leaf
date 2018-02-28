package com.leaf.common.model;

public class ServiceWrapper {

    private static final int DEFAULT_WEIGHT = 50;

    private ServiceMeta serviceMeta;

    private Object serviceProvider;

    private int weight = DEFAULT_WEIGHT;

    public ServiceWrapper(String group, String providerName, String version, Object serviceProvider) {
        this(group, providerName, version, serviceProvider, DEFAULT_WEIGHT);
    }

    public ServiceWrapper(String group, String providerName, String version, Object serviceProvider, int weight) {
        this.serviceMeta = new ServiceMeta(group, providerName, version);
        this.serviceProvider = serviceProvider;
        this.weight = weight;
    }

    public ServiceMeta getServiceMeta() {
        return serviceMeta;
    }

    public void setServiceMeta(ServiceMeta serviceMeta) {
        this.serviceMeta = serviceMeta;
    }

    public Object getServiceProvider() {
        return serviceProvider;
    }

    public void setServiceProvider(Object serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
