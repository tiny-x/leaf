package com.leaf.rpc.provider.process;

import java.io.Serializable;

/**
 * 响应包装，序列化后生产的对象
 */
public class ResponseWrapper implements Serializable {

    private static final long serialVersionUID = -1126932930252953428L;

    private Object result;

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public void setCase(Throwable cause) {
        this.result = cause;
    }

    @Override
    public String toString() {
        return "ResponseWrapper{" +
                "result=" + result +
                '}';
    }
}
