package com.leaf.register.api.model;

import com.leaf.common.UnresolvedAddress;
import com.leaf.common.model.ServiceMeta;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegisterMeta that = (RegisterMeta) o;

        if (getConnCount() != that.getConnCount()) return false;
        if (getWeight() != that.getWeight()) return false;
        if (!getServiceMeta().equals(that.getServiceMeta())) return false;
        return getAddress().equals(that.getAddress());
    }

    @Override
    public int hashCode() {
        int result = getServiceMeta().hashCode();
        result = 31 * result + getAddress().hashCode();
        result = 31 * result + getConnCount();
        result = 31 * result + getWeight();
        return result;
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
