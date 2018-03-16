package com.leaf.remoting.api.payload;

import com.leaf.remoting.api.ProtocolHead;
import com.leaf.common.utils.AnyThrow;

import java.util.concurrent.atomic.AtomicLong;

public class RequestCommand extends ByteHolder implements Cloneable {

    private static final AtomicLong REQUEST_ID = new AtomicLong(0L);

    private long timestamp;

    private long invokeId;

    public RequestCommand(byte serializerCode, byte[] body) {
        this(ProtocolHead.RPC_REQUEST, serializerCode, body);
    }

    public RequestCommand(byte messageCode, byte serializerCode, byte[] body) {
        this(messageCode, serializerCode, body, REQUEST_ID.incrementAndGet());
    }

    public RequestCommand(byte messageCode, byte serializerCode, byte[] body, Long invokeId) {
        super(messageCode, serializerCode, body);
        this.invokeId = invokeId;
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getInvokeId() {
        return invokeId;
    }

    public void markOneWay() {
        super.messageCode = ProtocolHead.ONEWAY_REQUEST;
    }

    public boolean isOneWay() {
        return super.messageCode == ProtocolHead.ONEWAY_REQUEST;
    }

    @Override
    public String toString() {
        return "RequestCommand{" +
                "timestamp=" + timestamp +
                ", invokeId=" + invokeId +
                ", messageCode=" + messageCode +
                '}';
    }

    @Override
    public RequestCommand clone() {
        RequestCommand clone = null;
        try {
            clone = (RequestCommand) super.clone();
        } catch (CloneNotSupportedException e) {
            AnyThrow.throwUnchecked(e);
        }
        clone.invokeId = REQUEST_ID.incrementAndGet();
        clone.timestamp = System.currentTimeMillis();
        return clone;
    }
}
