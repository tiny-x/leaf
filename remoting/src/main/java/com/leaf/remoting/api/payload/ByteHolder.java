package com.leaf.remoting.api.payload;

public abstract class ByteHolder {

    protected byte messageCode;

    private byte serializerCode;

    private byte[] body;

    public ByteHolder(byte messageCode, byte serializerCode, byte[] body) {
        this.messageCode = messageCode;
        this.serializerCode = serializerCode;
        this.body = body;
    }

    public byte getSerializerCode() {
        return serializerCode;
    }

    public byte[] getBody() {
        return body;
    }

    public byte getMessageCode() {
        return messageCode;
    }
}
