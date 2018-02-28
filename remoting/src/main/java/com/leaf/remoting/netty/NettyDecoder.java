package com.leaf.remoting.netty;

import com.leaf.common.ProtocolHead;
import com.leaf.remoting.api.RemotingCommandFactory;
import com.leaf.remoting.api.payload.RequestCommand;
import com.leaf.remoting.api.payload.ResponseCommand;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;


import java.util.List;

public class NettyDecoder extends ReplayingDecoder<NettyDecoder.State> {

    public NettyDecoder() {
        super(State.HEADER_MAGIC);
    }

    private final ProtocolHead head = new ProtocolHead();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state()) {
            case HEADER_MAGIC:
                checkMagic(in.readShort());
                checkpoint(State.HEADER_SIGN);
            case HEADER_SIGN:
                head.setSign(in.readByte());
                checkpoint(State.HEADER_STATUS);
            case HEADER_STATUS:
                head.setStatus(in.readByte());
                checkpoint(State.HEADER_ID);
            case HEADER_ID:
                head.setInvokeId(in.readLong());
                checkpoint(State.HEADER_BODY_LENGTH);
            case HEADER_BODY_LENGTH:
                head.setBodyLength(in.readInt());
                checkpoint(State.BODY);
            case BODY:
                boolean isRequest = ((head.getMessageCode() % 2) == 1);
                if (isRequest) {
                    byte[] body = new byte[head.getBodyLength()];
                    in.readBytes(body);
                    RequestCommand requestCommand = RemotingCommandFactory.createRequestCommand(
                            head.getMessageCode(),
                            head.getSerializerCode(),
                            body,
                            head.getInvokeId()
                    );

                    out.add(requestCommand);
                } else {
                    byte[] body = new byte[head.getBodyLength()];
                    in.readBytes(body);
                    ResponseCommand responseCommand = RemotingCommandFactory.createResponseCommand(
                            head.getMessageCode(),
                            head.getSerializerCode(),
                            body,
                            head.getInvokeId()
                    );

                    responseCommand.setStatus(head.getStatus());
                    out.add(responseCommand);
                }
                checkpoint(State.HEADER_MAGIC);
        }
    }

    private void checkMagic(Short magic) {
        if (magic != ProtocolHead.MAGIC) {
            throw new UnsupportedOperationException("unsupported" + Integer.toHexString(magic));
        }
    }

    enum State {
        HEADER_MAGIC,
        HEADER_SIGN,
        HEADER_STATUS,
        HEADER_ID,
        HEADER_BODY_LENGTH,
        BODY
    }
}
