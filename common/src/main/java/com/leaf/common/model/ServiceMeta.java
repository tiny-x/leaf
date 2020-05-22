package com.leaf.common.model;

import com.leaf.common.constants.Constants;

/**
 * 服务三元素 确定服务
 */
public class ServiceMeta extends Directory {

    private String group;

    private String serviceProviderName;

    private String version;

    public ServiceMeta() {
    }

    public ServiceMeta(String serviceProviderName) {
        this(Constants.DEFAULT_SERVICE_GROUP, serviceProviderName);
    }

    public ServiceMeta(String group, String serviceProviderName) {
        this(group, serviceProviderName, Constants.DEFAULT_SERVICE_VERSION);
    }

    public ServiceMeta(String group, String serviceProviderName, String version) {
        this.group = group;
        this.serviceProviderName = serviceProviderName;
        this.version = version;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public String getServiceProviderName() {
        return serviceProviderName;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "ServiceMeta{" +
                "group='" + group + '\'' +
                ", serviceProviderName='" + serviceProviderName + '\'' +
                ", version='" + version + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceMeta that = (ServiceMeta) o;

        if (group != null ? !group.equals(that.group) : that.group != null) return false;
        if (serviceProviderName != null ? !serviceProviderName.equals(that.serviceProviderName) : that.serviceProviderName != null)
            return false;
        return version != null ? version.equals(that.version) : that.version == null;
    }

    @Override
    public int hashCode() {
        int result = group != null ? group.hashCode() : 0;
        result = 31 * result + (serviceProviderName != null ? serviceProviderName.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }
}
