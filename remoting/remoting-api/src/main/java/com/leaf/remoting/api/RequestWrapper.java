package com.leaf.remoting.api;

import com.leaf.common.model.ServiceMeta;

import java.io.Serializable;
import java.util.Map;

/**
 * 请求 包装, 用与序列化
 * @author yefei
 */
public class RequestWrapper implements Serializable {

    private static final long serialVersionUID = -1126932930252953428L;

    private String application;

    private ServiceMeta serviceMeta;

    private String methodName;

    private Object[] args;

    private Map<String, String> attachment;

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

    public Map<String, String> getAttachment() {
        return attachment;
    }

    public void setAttachment(Map<String, String> attachment) {
        this.attachment = attachment;
    }
}
