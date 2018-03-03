package com.leaf.remoting.api.payload;

import com.leaf.common.ProtocolHead;
import com.leaf.remoting.api.ResponseStatus;

public class ResponseCommand extends ByteHolder {

    /**
     * 响应状态
     */
    private byte status;

    private long invokeId;

    public ResponseCommand(byte serializerCode, byte[] body, long invokeId) {
        this(ProtocolHead.RESPONSE, serializerCode, body, invokeId);
    }

    public ResponseCommand(byte messageCode, byte serializerCode, byte[] body, long invokeId) {
        super(messageCode, serializerCode, body);
        this.status = ResponseStatus.SUCCESS.value();
        this.invokeId = invokeId;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public void setInvokeId(long invokeId) {
        this.invokeId = invokeId;
    }

    public byte getStatus() {
        return status;
    }

    public long getInvokeId() {
        return invokeId;
    }

    @Override
    public String toString() {
        return "ResponseCommand{" +
                "status=" + status +
                ", invokeId=" + invokeId +
                ", messageCode=" + messageCode +
                '}';
    }
}
