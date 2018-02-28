package com.leaf.common.model;

/**
 * 请求 包装
 */
public class RequestWrapper {

    private String application;

    private ServiceMeta serviceMeta;

    private String methodName;

    private Object[] args;

    public ServiceMeta getServiceMeta() {
        return serviceMeta;
    }

    public void setServiceMeta(ServiceMeta serviceMeta) {
        this.serviceMeta = serviceMeta;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }
}
