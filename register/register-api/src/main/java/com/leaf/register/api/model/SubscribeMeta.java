package com.leaf.register.api.model;

import com.leaf.common.model.ServiceMeta;

public class SubscribeMeta {

    private ServiceMeta serviceMeta ;

    private String addressHost;

    public SubscribeMeta() {
    }

    public SubscribeMeta(ServiceMeta serviceMeta, String addressHost) {
        this.serviceMeta = serviceMeta;
        this.addressHost = addressHost;
    }

    public ServiceMeta getServiceMeta() {
        return serviceMeta;
    }

    public void setServiceMeta(ServiceMeta serviceMeta) {
        this.serviceMeta = serviceMeta;
    }

    public String getAddressHost() {
        return addressHost;
    }

    public void setAddressHost(String addressHost) {
        this.addressHost = addressHost;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("SubscribeMeta{");
        sb.append("serviceMeta=").append(serviceMeta);
        sb.append(", addressHost='").append(addressHost).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SubscribeMeta that = (SubscribeMeta) o;

        if (getServiceMeta() != null ? !getServiceMeta().equals(that.getServiceMeta()) : that.getServiceMeta() != null)
            return false;
        return getAddressHost() != null ? getAddressHost().equals(that.getAddressHost()) : that.getAddressHost() == null;
    }

    @Override
    public int hashCode() {
        int result = getServiceMeta() != null ? getServiceMeta().hashCode() : 0;
        result = 31 * result + (getAddressHost() != null ? getAddressHost().hashCode() : 0);
        return result;
    }
}
