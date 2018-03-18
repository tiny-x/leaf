package com.leaf.remoting.api;


/**
 *   *                                          ProtocolHead
 *  ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐
 *       2   │   1   │    1   │     8     │      4      │
 *  ├ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┤
 *           │       │        │           │             │
 *  │  MAGIC   Sign    Status   Invoke Id   Body Length                   Body Content              │
 *           │       │        │           │             │
 *  └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘
 *
 */
public class ProtocolHead {

    public static final int HEADER_SIZE = 16;

    public static final short MAGIC = (short) (0xcaff);

    /**
     * messageType 1bit
     *
     */
    public static final byte REQUEST =      0x00;
    public static final byte RESPONSE =     0x01;

    /**
     * messageCode 4bit 0x00~0x00
     */
    public static final byte HEARTBEAT =                0x00;   // 心跳包
    public static final byte RPC_REQUEST =              0x01;   // rpc request
    public static final byte PRC_RESPONSE =             0x02;   // rpc response
    public static final byte REGISTER_SERVICE =         0x03;   // 注册服务
    public static final byte ACK =                      0x04;   // ack
    public static final byte SUBSCRIBE_SERVICE =        0x05;   // 监听服务
    public static final byte SUBSCRIBE_RECEIVE =        0x06;   // 监听服务
    public static final byte OFFLINE_SERVICE =          0x07;   // 服务端下线
    public static final byte CANCEL_REGISTER_SERVICE =  0x09;   // 取消注册服务
    public static final byte ONEWAY_REQUEST =           0x0A;   // 单向调用
    public static final byte LOOKUP_SERVICE =           0x0B;   // 查找服务

    /**
     * serializerCode 3bit
     */
    private byte serializerCode;

    /**
     * 消息类型（高一位） 消息标识（高四位），序列化方式（低三位）
     */
    private byte sign;

    private byte messageType;

    private byte messageCode;

    private byte status;

    private long invokeId;

    private int bodyLength;

    public ProtocolHead() {

    }

    public static byte toSign(byte messageType, byte messageCode, byte serializerCode) {
        return (byte) (
                (messageType << 7) | (messageCode << 3) | (serializerCode >> 1)
        );
    }

    public void setSign(byte sign) {
        this.sign = sign;
        this.messageType = (byte) ((sign & 0x80) >> 7);
        this.messageCode = (byte) ((sign & 0x78) >> 3);
        this.serializerCode = (byte) (sign & 0x07);
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public long getInvokeId() {
        return invokeId;
    }

    public void setInvokeId(long invokeId) {
        this.invokeId = invokeId;
    }

    public int getBodyLength() {
        return bodyLength;
    }

    public void setBodyLength(int bodyLength) {
        this.bodyLength = bodyLength;
    }

    public byte getMessageCode() {
        return messageCode;
    }

    public byte getSerializerCode() {
        return serializerCode;
    }

    public byte getMessageType() {
        return messageType;
    }
}
