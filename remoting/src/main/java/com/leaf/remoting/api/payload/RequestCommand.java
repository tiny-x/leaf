package com.leaf.remoting.api.payload;

import com.leaf.common.ProtocolHead;

import java.util.concurrent.atomic.AtomicLong;

public class RequestCommand extends ByteHolder {

    private static final AtomicLong REQUEST_ID = new AtomicLong(0L);

    private long timestamp;

    private long invokeId;

    public RequestCommand(byte serializerCode, byte[] body) {
        this(ProtocolHead.REQUEST, serializerCode, body);
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

    public long getAndIncrement() {
        return (invokeId = REQUEST_ID.getAndIncrement());
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
}
