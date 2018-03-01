package com.leaf.remoting.netty;

import com.leaf.common.model.Pair;
import com.leaf.common.model.ResponseWrapper;
import com.leaf.remoting.api.*;
import com.leaf.remoting.api.future.ResponseFuture;
import com.leaf.remoting.api.payload.ByteHolder;
import com.leaf.remoting.api.payload.RequestCommand;
import com.leaf.remoting.api.payload.ResponseCommand;
import com.leaf.remoting.exception.RemotingException;
import com.leaf.remoting.exception.RemotingSendRequestException;
import com.leaf.remoting.exception.RemotingTimeoutException;
import com.leaf.remoting.exception.RemotingTooMuchRequestException;
import com.leaf.remoting.netty.event.ChannelEvent;
import com.leaf.serialization.api.Serializer;
import com.leaf.serialization.api.SerializerFactory;
import com.leaf.serialization.api.SerializerType;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.*;


public abstract class NettyServiceAbstract {

    private static final Logger logger = LoggerFactory.getLogger(NettyServiceAbstract.class);

    protected final ConcurrentMap<Long, ResponseFuture<ResponseCommand>> responseTable =
            new ConcurrentHashMap(256);

    protected final HashMap<Integer/* request code */, Pair<RequestProcessor, ExecutorService>> processorTable =
            new HashMap(64);

    protected final Semaphore semaphoreAsync;

    protected final Semaphore semaphoreOneWay;

    protected final Pair<RequestProcessor, ExecutorService> defaultProcessor = new Pair();

    protected final ChannelEventExecutor channelEventExecutor = new ChannelEventExecutor();

    public NettyServiceAbstract(final int permitsAsync, final int permitsOneWay) {
        this.semaphoreAsync = new Semaphore(permitsAsync, true);
        this.semaphoreOneWay = new Semaphore(permitsOneWay, true);
    }

    public void processMessageReceived(ChannelHandlerContext ctx, ByteHolder msg) throws Exception {
        final ByteHolder cmd = msg;
        if (cmd != null) {
            if (msg instanceof RequestCommand) {
                processRequestCommand(ctx, (RequestCommand) msg);
            } else if (msg instanceof ResponseCommand) {
                processResponseCommand(ctx, (ResponseCommand) msg);
            }
        }
    }

    private void processResponseCommand(ChannelHandlerContext ctx, ResponseCommand cmd) throws Exception {
        long invokeId = cmd.getInvokeId();
        ResponseFuture<ResponseCommand> future = responseTable.get(invokeId);
        if (future != null) {
            future.complete(cmd);
            if (future.getInvokeCallback() != null) {
                responseTable.remove(invokeId);
                future.executeInvokeCallback();
            }
        } else {
            logger.warn("receive response, but not matched any request, " + ctx.channel());
            logger.warn(cmd.toString());
        }
    }

    private void processRequestCommand(ChannelHandlerContext ctx, RequestCommand cmd) {
        Serializer serializer = SerializerFactory.serializer(SerializerType.parse(cmd.getSerializerCode()));
        ResponseWrapper responseWrapper = new ResponseWrapper();

        if (defaultProcessor.getA() != null && defaultProcessor.getB() != null) {
            try {
                defaultProcessor.getB().submit(() -> {
                    ResponseCommand responseCommand = null;
                    if (defaultProcessor.getA().rejectRequest()) {
                        String message = "[REJECT_REQUEST] system busy, start flow control for a while";
                        responseWrapper.setResult(message);
                        logger.warn(message);
                        if (!cmd.isOneWay()) {
                            responseCommand = RemotingCommandFactory.createResponseCommand(
                                    cmd.getSerializerCode(),
                                    serializer.serialize(responseWrapper),
                                    cmd.getInvokeId()
                            );
                            responseCommand.setStatus(ResponseStatus.FLOW_CONTROL.value());
                        }
                    } else {
                        responseCommand = defaultProcessor.getA().process(ctx, cmd);
                    }
                    if (responseCommand != null) {
                        ctx.channel().writeAndFlush(responseCommand);
                    }
                });
            } catch (RejectedExecutionException e) {

                String message = "[OVERLOAD]system busy, start flow control for a while";
                responseWrapper.setResult(message);
                logger.error(message, e);

                if (!cmd.isOneWay()) {
                    ResponseCommand responseCommand = RemotingCommandFactory.createResponseCommand(
                            cmd.getSerializerCode(),
                            serializer.serialize(responseWrapper),
                            cmd.getInvokeId()
                    );
                    responseCommand.setStatus(ResponseStatus.SYSTEM_BUSY.value());
                    ctx.channel().writeAndFlush(responseCommand);
                }
            }
        } else {
            String message = "[ERROR]system error, request process not register";
            responseWrapper.setResult(message);
            logger.error(ctx.channel() + message);

            if (!cmd.isOneWay()) {
                ResponseCommand responseCommand = RemotingCommandFactory.createResponseCommand(
                        cmd.getSerializerCode(),
                        serializer.serialize(responseWrapper),
                        cmd.getInvokeId()
                );
                responseCommand.setStatus(ResponseStatus.SERVER_ERROR.value());
                ctx.channel().writeAndFlush(responseCommand);
            }
        }
    }

    protected ResponseCommand invokeSync0(Channel channel, RequestCommand request, long timeout, TimeUnit timeUnit)
            throws RemotingException, InterruptedException {
        ResponseFuture<ResponseCommand> responseFuture = new ResponseFuture<>();
        responseTable.putIfAbsent(request.getInvokeId(), responseFuture);
        try {
            channel.writeAndFlush(request).addListener((ChannelFuture future) -> {
                responseFuture.setSuccess(future.isSuccess());
                if (!future.isSuccess()) {
                    responseTable.remove(request.getInvokeId());
                    responseFuture.complete(null);
                    responseFuture.failure(future.cause());
                    logger.warn("send a request command to channel <" + channel + "> failed.");
                }
            });
            ResponseCommand response = responseFuture.get(timeout, timeUnit);
            if (response == null) {
                if (responseFuture.isSuccess()) {
                    throw new RemotingTimeoutException(channel.remoteAddress().toString(),
                            timeUnit.convert(timeout, TimeUnit.MILLISECONDS));
                } else {
                    throw new RemotingSendRequestException("send request failed", responseFuture.cause());
                }
            }
            return response;
        } finally {
            responseTable.remove(request.getInvokeId());
        }
    }

    protected void invokeAsync0(Channel channel, RequestCommand request,
                                 long timeout, TimeUnit timeUnit, InvokeCallback<ResponseCommand> invokeCallback)
            throws RemotingException, InterruptedException {

        ResponseFuture<ResponseCommand> responseFuture = new ResponseFuture<>(invokeCallback);
        responseTable.putIfAbsent(request.getInvokeId(), responseFuture);

        if (semaphoreAsync.tryAcquire(timeout, timeUnit)) {

            channel.writeAndFlush(request).addListener((ChannelFuture future) -> {
                responseFuture.setSuccess(future.isSuccess());
                if (!future.isSuccess()) {
                    responseTable.remove(request.getInvokeId());
                    responseFuture.complete(null);
                    responseFuture.executeInvokeCallback();
                    responseFuture.failure(future.cause());
                    logger.warn("send a request command to channel <" + channel + "> failed.");
                }
            });
        } else {
            if (timeout <= 0) {
                throw new RemotingTooMuchRequestException("invokeAsyncImpl invoke too fast");
            } else {
                String info =
                        String.format("invokeAsync tryAcquire semaphore timeout, %dms, waiting thread nums: %d semaphoreAsyncValue: %d",
                                timeout,
                                this.semaphoreAsync.getQueueLength(),
                                this.semaphoreAsync.availablePermits()
                        );
                logger.warn(info);
                throw new RemotingTimeoutException(info);
            }
        }
    }


    protected void invokeOneWay0(Channel channel, RequestCommand request, long timeout, TimeUnit timeUnit)
            throws RemotingException, InterruptedException {
        request.markOneWay();
        if (semaphoreAsync.tryAcquire(timeout, timeUnit)) {

            channel.writeAndFlush(request).addListener((ChannelFuture future) -> {
                if (!future.isSuccess()) {
                    logger.warn("send a request command to channel <" + channel + "> failed.");
                }
            });
        } else {
            if (timeout <= 0) {
                throw new RemotingTooMuchRequestException("invokeAsyncImpl invoke too fast");
            } else {
                String info =
                        String.format("invokeOneWay tryAcquire semaphore timeout, %dms, waiting thread nums: %d semaphoreOneWay: %d",
                                timeout,
                                this.semaphoreOneWay.getQueueLength(),
                                this.semaphoreOneWay.availablePermits()
                        );
                logger.warn(info);
                throw new RemotingTimeoutException(info);
            }
        }
    }

    protected abstract ChannelEventListener getChannelEventListener();

    class ChannelEventExecutor implements Runnable {

        private final LinkedBlockingQueue<ChannelEvent> eventQueue = new LinkedBlockingQueue<>();
        private final int maxSize = 10000;

        public void putChannelEvent(final ChannelEvent event) {
            if (this.eventQueue.size() <= maxSize) {
                this.eventQueue.add(event);
            } else {
                logger.warn("event queue size[{}] enough, so drop this event {}", this.eventQueue.size(), event.toString());
            }
        }

        @Override
        public void run() {
            final ChannelEventListener listener = NettyServiceAbstract.this.getChannelEventListener();

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    ChannelEvent event = this.eventQueue.poll(3000, TimeUnit.MILLISECONDS);
                    if (event != null && listener != null) {
                        switch (event.getType()) {
                            case IDLE:
                                listener.onChannelIdle(event.getRemoteAddr(), event.getChannel());
                                break;
                            case CLOSE:
                                listener.onChannelClose(event.getRemoteAddr(), event.getChannel());
                                break;
                            case CONNECT:
                                listener.onChannelConnect(event.getRemoteAddr(), event.getChannel());
                                break;
                            case ACTIVE:
                                listener.onChannelActive(event.getRemoteAddr(), event.getChannel());
                                break;
                            case INACTIVE:
                                listener.onChannelInActive(event.getRemoteAddr(), event.getChannel());
                                break;
                            case EXCEPTION:
                                listener.onChannelException(event.getRemoteAddr(), event.getChannel());
                                break;
                            default:
                                break;
                        }
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        }

    }

}
