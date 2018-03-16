package com.leaf.remoting.netty;

import com.leaf.remoting.api.ProtocolHead;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class Heartbeats {

    private static final ByteBuf HEARTBEAT_BUF;

    static {
        ByteBuf buf = Unpooled.buffer(ProtocolHead.HEADER_SIZE);
        buf.writeShort(ProtocolHead.MAGIC);
        buf.writeByte(ProtocolHead.HEARTBEAT << 4); // 忽略序列化低4位
        buf.writeByte(0);
        buf.writeLong(0);
        buf.writeInt(0);
        HEARTBEAT_BUF = Unpooled.unreleasableBuffer(buf).asReadOnly();
    }

    public static ByteBuf heartbeatContent() {
        return HEARTBEAT_BUF.duplicate();
    }
}