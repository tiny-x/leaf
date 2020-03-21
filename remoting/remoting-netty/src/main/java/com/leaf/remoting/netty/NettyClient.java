package com.leaf.remoting.netty;

import com.leaf.common.UnresolvedAddress;
import com.leaf.common.model.Directory;
import com.leaf.common.utils.Collections;
import com.leaf.common.utils.Maps;
import com.leaf.remoting.api.*;
import com.leaf.remoting.api.channel.ChannelGroup;
import com.leaf.remoting.api.channel.DirectoryChannelGroup;
import com.leaf.remoting.api.exception.RemotingConnectException;
import com.leaf.remoting.api.exception.RemotingConnectTimeoutException;
import com.leaf.remoting.api.exception.RemotingException;
import com.leaf.remoting.api.payload.RequestCommand;
import com.leaf.remoting.api.payload.ResponseCommand;
import com.leaf.remoting.channel.NettyChannelGroup;
import com.leaf.remoting.netty.event.ChannelEvent;
import com.leaf.remoting.netty.handler.client.NettyClientHandler;
import com.leaf.remoting.netty.handler.client.NettyConnectManageHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkNotNull;

public class NettyClient extends NettyServiceAbstract implements RemotingClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    private final Bootstrap bootstrap = new Bootstrap();

    private final NettyEncoder encoder = new NettyEncoder();

    private final NettyConnectManageHandler nettyConnectManageHandler;

    private final NioEventLoopGroup nioEventLoopGroupWorker = new NioEventLoopGroup();

    private final NettyClientConfig config;

    private final ConcurrentMap<String, ChannelGroup> addressGroups = new ConcurrentHashMap<>();

    private final DirectoryChannelGroup directoryChannelGroup = new DirectoryChannelGroup();

    private final ChannelEventListener channelEventListener;

    private final ExecutorService publicExecutorService;

    private final ScheduledExecutorService scanResponseTableExecutorService;

    private final ConcurrentMap<UnresolvedAddress, CopyOnWriteArrayList<Connector>> connectorsMap = Maps.newConcurrentMap();

    public NettyClient(NettyClientConfig config) {
        this(config, null);
    }

    public NettyClient(NettyClientConfig config, ChannelEventListener listener) {
        super(config.getClientAsyncSemaphoreValue(), config.getClientOnewaySemaphoreValue());
        this.config = config;
        this.channelEventListener = listener;
        this.nettyConnectManageHandler = new NettyConnectManageHandler(this);

        this.publicExecutorService = Executors.newFixedThreadPool(AVAILABLE_PROCESSORS, new ThreadFactory() {

            AtomicInteger atomicInteger = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("PUBLIC#EXECUTOR#" + atomicInteger);
                return thread;
            }
        });

        scanResponseTableExecutorService = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("SCAN#PRC_RESPONSE#TABLE");
                return thread;
            }
        });
    }

    @Override
    public Connector connect(UnresolvedAddress address) throws InterruptedException, RemotingConnectException {
        checkNotNull(address);

        ChannelFuture future = bootstrap.connect(address.getHost(), address.getPort());

        if (future.awaitUninterruptibly(config.getConnectTimeoutMillis(), TimeUnit.MILLISECONDS)) {
            if (future.channel() != null && future.channel().isActive()) {

                CopyOnWriteArrayList<Connector> connectors = this.connectorsMap.get(address);
                if (connectors == null) {
                    CopyOnWriteArrayList<Connector> newConnectors = new CopyOnWriteArrayList<>();
                    connectors = connectorsMap.putIfAbsent(address, newConnectors);
                    if (connectors == null) {
                        connectors = newConnectors;
                    }
                }
                Connector connector = new Connector(address);
                connectors.add(connector);

                // 连接成功 添加channel到channelGroup
                future.channel().pipeline()
                        .addLast(new NettyClientHandler(connector, bootstrap, this));

                group(address).addChannel(future.channel());
                logger.info("connect with: {}", future.channel());

                return connector;
            } else {
                throw new RemotingConnectException(address.toString());
            }
        } else {
            throw new RemotingConnectTimeoutException(address.toString());
        }
    }

    @Override
    public ChannelGroup group(UnresolvedAddress address) {

        ChannelGroup group = addressGroups.get(address);
        if (group == null) {
            ChannelGroup newGroup = new NettyChannelGroup(address);
            group = addressGroups.putIfAbsent(address.toString(), newGroup);
            if (group == null) {
                group = newGroup;
            }
        }
        return group;
    }

    @Override
    public boolean hasAvailableChannelGroup(UnresolvedAddress address) {
        return group(address).isAvailable();
    }

    @Override
    public boolean addChannelGroup(Directory directory, UnresolvedAddress address) {
        ChannelGroup group = group(address);
        CopyOnWriteArrayList groups = directoryChannelGroup.find(directory);
        boolean added = groups.addIfAbsent(group);
        if (added) {
            if (logger.isInfoEnabled()) {
                logger.info("Added channel group: {} to {}.", group, directory.directory());
            }
        }
        return added;
    }

    @Override
    public boolean removeChannelGroup(Directory directory, UnresolvedAddress address) {
        ChannelGroup group = group(address);
        CopyOnWriteArrayList groups = directoryChannelGroup.find(directory);
        boolean removed = groups.remove(group);
        if (removed) {
            if (logger.isInfoEnabled()) {
                logger.info("Removed channel group: {} to {}.", group, directory.directory());
            }
        }
        return removed;
    }

    @Override
    public CopyOnWriteArrayList<ChannelGroup> directory(Directory directory) {
        return directoryChannelGroup.find(directory);
    }

    @Override
    public boolean isDirectoryAvailable(Directory directory) {
        CopyOnWriteArrayList<ChannelGroup> groups = directory(directory);
        for (ChannelGroup g : groups) {
            if (g.isAvailable()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ResponseCommand invokeSync(Channel channel, RequestCommand request, long timeoutMillis)
            throws RemotingException, InterruptedException {
        return invokeSync0(channel, request, timeoutMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void invokeAsync(Channel channel, RequestCommand request, long timeoutMillis, InvokeCallback<ResponseCommand> invokeCallback)
            throws RemotingException, InterruptedException {
        invokeAsync0(channel, request, timeoutMillis, TimeUnit.MILLISECONDS, invokeCallback);
    }

    @Override
    public ResponseCommand invokeSync(final UnresolvedAddress address, RequestCommand request, long timeoutMillis)
            throws RemotingException, InterruptedException {
        return invokeSync0(group(address).next(), request, timeoutMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void invokeAsync(final UnresolvedAddress address, RequestCommand request,
                            long timeoutMillis, InvokeCallback<ResponseCommand> invokeCallback)
            throws RemotingException, InterruptedException {
        invokeAsync0(group(address).next(), request, timeoutMillis, TimeUnit.MILLISECONDS, invokeCallback);
    }

    @Override
    public void invokeOneWay(Channel channel, RequestCommand request, long timeoutMillis)
            throws RemotingException, InterruptedException {
        invokeOneWay0(channel, request, timeoutMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void invokeOneWay(UnresolvedAddress address, RequestCommand request, long timeoutMillis)
            throws RemotingException, InterruptedException {
        invokeOneWay0(group(address).next(), request, timeoutMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void registerRequestProcess(RequestProcessor requestProcessor, ExecutorService executor) {
        defaultProcessor.setA(requestProcessor);
        defaultProcessor.setB(executor);
    }

    @Override
    protected ExecutorService publicExecutorService() {
        return publicExecutorService;
    }

    @Override
    public void start() {
        bootstrap.group(nioEventLoopGroupWorker)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .option(ChannelOption.SO_SNDBUF, config.getClientSocketSndBufSize())
                .option(ChannelOption.SO_RCVBUF, config.getClientSocketRcvBufSize())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(
                                new IdleStateHandler(0, config.getIdleWriteSeconds(), config.getIdleAllSeconds()),
                                encoder,
                                new NettyDecoder(),
                                nettyConnectManageHandler
                        );
                    }
                });

        this.scanResponseTableExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    scanResponseTable();
                } catch (Throwable e) {
                    logger.error("scanResponseTable exception", e);
                }
            }
        }, 1000 * 3, 1000, TimeUnit.MILLISECONDS);

        if (channelEventListener != null) {
            new Thread(channelEventExecutor).start();
        }
    }

    public void putChannelEvent(ChannelEvent channelEvent) {
        this.channelEventExecutor.putChannelEvent(channelEvent);
    }

    @Override
    public ChannelEventListener getChannelEventListener() {
        return this.channelEventListener;
    }

    @Override
    public void cancelReconnect(UnresolvedAddress address) {
        CopyOnWriteArrayList<Connector> connectors = connectorsMap.get(address);
        if (Collections.isNotEmpty(connectors)) {
            for (Connector connector : connectors) {
                connector.setNeedReconnect(false);
            }
        }
    }

    @Override
    public void shutdownGracefully() {
        nioEventLoopGroupWorker.shutdownGracefully().syncUninterruptibly();

        if (publicExecutorService != null) {
            publicExecutorService.shutdown();
        }
        if (scanResponseTableExecutorService != null) {
            scanResponseTableExecutorService.shutdown();
        }

    }
}
