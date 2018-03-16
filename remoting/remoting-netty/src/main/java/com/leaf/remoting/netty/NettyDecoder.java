package com.leaf.remoting.netty;

import com.leaf.remoting.api.ProtocolHead;
import com.leaf.remoting.api.RemotingCommandFactory;
import com.leaf.remoting.api.payload.RequestCommand;
import com.leaf.remoting.api.payload.ResponseCommand;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class NettyDecoder extends ReplayingDecoder<NettyDecoder.State> {

    private final ProtocolHead head = new ProtocolHead();

    public NettyDecoder() {
        super(State.HEADER_MAGIC);
    }

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
                byte[] body = new byte[head.getBodyLength()];
                in.readBytes(body);
                switch (head.getMessageType()) {
                    case ProtocolHead.REQUEST: {
                        if (head.getMessageCode() == ProtocolHead.HEARTBEAT)
                            break;

                        RequestCommand requestCommand = RemotingCommandFactory.createRequestCommand(
                                head.getMessageCode(),
                                head.getSerializerCode(),
                                body,
                                head.getInvokeId()
                        );
                        out.add(requestCommand);
                        break;
                    }
                    case ProtocolHead.RESPONSE: {
                        ResponseCommand responseCommand = RemotingCommandFactory.createResponseCommand(
                                head.getMessageCode(),
                                head.getSerializerCode(),
                                body,
                                head.getInvokeId()
                        );
                        responseCommand.setStatus(head.getStatus());
                        out.add(responseCommand);
                        break;
                    }
                }
                checkpoint(State.HEADER_MAGIC);
        }
    }

    private void checkMagic(Short magic) {
        if (magic != ProtocolHead.MAGIC) {
            throw new UnsupportedOperationException("unsupported magic: " + Integer.toHexString(magic));
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
