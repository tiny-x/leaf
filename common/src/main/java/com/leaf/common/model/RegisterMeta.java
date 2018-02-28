package com.leaf.common.model;

import com.leaf.common.UnresolvedAddress;

public class RegisterMeta {

    private ServiceMeta serviceMeta ;

    private UnresolvedAddress address;

    private volatile int connCount;

    private volatile int weight;

    public RegisterMeta() {
    }

    public ServiceMeta getServiceMeta() {
        return serviceMeta;
    }

    public void setServiceMeta(ServiceMeta serviceMeta) {
        this.serviceMeta = serviceMeta;
    }

    public UnresolvedAddress getAddress() {
        return address;
    }

    public void setAddress(UnresolvedAddress address) {
        this.address = address;
    }

    public int getConnCount() {
        return connCount;
    }

    public void setConnCount(int connCount) {
        this.connCount = connCount;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "RegisterMeta{" +
                "serviceMeta=" + serviceMeta +
                ", address=" + address +
                ", connCount=" + connCount +
                ", weight=" + weight +
                '}';
    }
}
