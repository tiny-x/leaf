package com.leaf.remoting.netty.handler.client;

import com.leaf.remoting.netty.Heartbeats;
import com.leaf.remoting.netty.NettyClient;
import com.leaf.remoting.netty.event.ChannelEvent;
import com.leaf.remoting.netty.event.ChannelEventType;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

@ChannelHandler.Sharable
public class NettyConnectManageHandler extends ChannelDuplexHandler {

    private final static Logger logger = LoggerFactory.getLogger(NettyConnectManageHandler.class);

    private NettyClient nettyClient;

    public NettyConnectManageHandler(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
                        ChannelPromise promise) throws Exception {
        final String local = localAddress == null ? "UNKNOWN" : localAddress.toString();
        final String remote = remoteAddress == null ? "UNKNOWN" : remoteAddress.toString();
        logger.debug("NETTY CLIENT PIPELINE: CONNECT  {} => {}", local, remote);

        super.connect(ctx, remoteAddress, localAddress, promise);

        if (nettyClient.getChannelEventListener() != null) {
            nettyClient.putChannelEvent(new ChannelEvent(ChannelEventType.CONNECT, remote, ctx.channel()));
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddress = ctx.channel().remoteAddress().toString();
        logger.debug("NETTY SERVER PIPELINE: channelActive, the channel[{}]", remoteAddress);
        super.channelActive(ctx);

        if (nettyClient.getChannelEventListener() != null) {
            nettyClient.putChannelEvent(new ChannelEvent(ChannelEventType.ACTIVE, remoteAddress, ctx.channel()));
        }
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        final String remoteAddress = ctx.channel().remoteAddress().toString();
        logger.debug("NETTY CLIENT PIPELINE: DISCONNECT {}", remoteAddress);
        ctx.channel().close();
        super.disconnect(ctx, promise);

        if (nettyClient.getChannelEventListener() != null) {
            nettyClient.putChannelEvent(new ChannelEvent(ChannelEventType.CLOSE, remoteAddress, ctx.channel()));
        }
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        final String remoteAddress = ctx.channel().remoteAddress().toString();
        logger.debug("NETTY CLIENT PIPELINE: CLOSE {}", remoteAddress);
        super.close(ctx, promise);

        if (nettyClient.getChannelEventListener() != null) {
            nettyClient.putChannelEvent(new ChannelEvent(ChannelEventType.CLOSE, remoteAddress, ctx.channel()));
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            final String remoteAddress = ctx.channel().remoteAddress().toString();
            switch (event.state()) {
                case ALL_IDLE:
                    if (nettyClient.getChannelEventListener() != null) {
                        nettyClient.putChannelEvent(new ChannelEvent(ChannelEventType.ALL_IDLE, remoteAddress, ctx.channel()));
                    }
                    break;
                case WRITER_IDLE:
                    if (nettyClient.getChannelEventListener() != null) {
                        nettyClient.putChannelEvent(new ChannelEvent(ChannelEventType.WRITE_IDLE, remoteAddress, ctx.channel()));
                    }
                    ctx.channel().writeAndFlush(Heartbeats.heartbeatContent()).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            logger.debug("channel write idle , send heartbeat, isSuccess: {}", channelFuture.isSuccess());
                        }
                    });
                    break;
            }
        }
        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        final String remoteAddress = ctx.channel().remoteAddress().toString();
        logger.warn("NETTY CLIENT PIPELINE: exceptionCaught {}", remoteAddress);
        logger.error(cause.getMessage(), cause);
        ctx.channel().close();

        if (nettyClient.getChannelEventListener() != null) {
            nettyClient.putChannelEvent(new ChannelEvent(ChannelEventType.EXCEPTION, remoteAddress, ctx.channel()));
        }
    }
}