package com.leaf.remoting.netty;

import com.leaf.remoting.api.ProtocolHead;
import com.leaf.remoting.api.payload.ByteHolder;
import com.leaf.remoting.api.payload.RequestCommand;
import com.leaf.remoting.api.payload.ResponseCommand;
import com.leaf.remoting.api.exception.RemotingException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class NettyEncoder extends MessageToByteEncoder<ByteHolder> {

    private static final Logger logger = LoggerFactory.getLogger(NettyEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteHolder msg, ByteBuf out) throws Exception {
        try {
            if (msg instanceof RequestCommand) {
                doEncodeRequest((RequestCommand) msg, out);
            } else if (msg instanceof ResponseCommand) {
                doEncodeResponse((ResponseCommand) msg, out);
            } else {
                throw new RemotingException("not support byte holder" + msg.getClass());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            ctx.channel().close().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    logger.warn("encode fail! close channel, isSuccess: {}", future.isSuccess());
                }
            });
        }
    }

    private void doEncodeRequest(RequestCommand request, ByteBuf out) {
        byte sign = ProtocolHead.toSign(
                ProtocolHead.REQUEST,
                request.getMessageCode(),
                request.getSerializerCode()
        );
        long invokeId = request.getInvokeId();
        byte[] bytes = request.getBody();
        if (bytes == null) {
            bytes = new byte[]{0};
        }
        int length = bytes.length;

        out.writeShort(ProtocolHead.MAGIC)
                .writeByte(sign)
                .writeByte(0x00)
                .writeLong(invokeId)
                .writeInt(length)
                .writeBytes(bytes);
    }

    private void doEncodeResponse(ResponseCommand response, ByteBuf out) {

        byte sign = ProtocolHead.toSign(
                ProtocolHead.RESPONSE,
                response.getMessageCode(),
                response.getSerializerCode()
        );
        byte status = response.getStatus();
        long invokeId = response.getInvokeId();
        byte[] bytes = response.getBody();
        if (bytes == null) {
            bytes = new byte[]{0};
        }
        int length = bytes.length;
        out.writeShort(ProtocolHead.MAGIC)
                .writeByte(sign)
                .writeByte(status)
                .writeLong(invokeId)
                .writeInt(length)
                .writeBytes(bytes);
    }

}
