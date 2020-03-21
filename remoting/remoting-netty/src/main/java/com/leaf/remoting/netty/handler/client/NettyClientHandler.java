package com.leaf.remoting.netty.handler.client;

import com.leaf.common.UnresolvedAddress;
import com.leaf.remoting.api.Connector;
import com.leaf.remoting.api.channel.ChannelGroup;
import com.leaf.remoting.api.payload.ByteHolder;
import com.leaf.remoting.netty.NettyClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

@ChannelHandler.Sharable
public class NettyClientHandler extends SimpleChannelInboundHandler<ByteHolder> implements TimerTask {

    private final static Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);
    private Timer timer = new HashedWheelTimer();

    private Connector connector;

    private int reties;

    private Bootstrap bootstrap;

    private NettyClient nettyClient;

    private ChannelGroup channelGroup;

    public NettyClientHandler(Connector connector, Bootstrap bootstrap, NettyClient nettyClient) {
        this.connector = connector;
        this.bootstrap = bootstrap;
        this.nettyClient = nettyClient;
        this.channelGroup = nettyClient.group(connector.getAddress());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        reties = 0;
        channelGroup.addChannel(ctx.channel());
        logger.info("reconnect with: {}", ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteHolder msg) throws Exception {
        nettyClient.processMessageReceived(ctx, msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (connector.isNeedReconnect()) {
            if (reties < 10) {
                reties++;
            }
            timer.newTimeout(this, 2 << reties, TimeUnit.SECONDS);
        }
        super.channelInactive(ctx);
    }

    @Override
    public void run(Timeout timeout) throws Exception {
        if (!connector.isNeedReconnect()) {
            logger.warn("Cancel reconnecting with {}.", connector.getAddress());
            return;
        }
        reconnect();
    }

    /**
     * 重连
     */
    private void reconnect() {
        UnresolvedAddress address = connector.getAddress();

        bootstrap.connect(address.getHost(), address.getPort())
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        future.channel().pipeline().addLast(NettyClientHandler.this);
                        boolean isSuccess = future.isSuccess();
                        logger.warn("Reconnects with {}, {}.", address, isSuccess ? "succeed" : "failed");
                        if (!isSuccess) {
                            future.channel().pipeline().fireChannelInactive();
                        } else {
                            channelGroup.addChannel(future.channel());
                        }
                    }
                });
    }
}