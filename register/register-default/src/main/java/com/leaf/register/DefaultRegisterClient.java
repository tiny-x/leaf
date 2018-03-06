package com.leaf.register;

import com.leaf.common.ProtocolHead;
import com.leaf.common.UnresolvedAddress;
import com.leaf.common.concurrent.ConcurrentSet;
import com.leaf.common.constants.Constants;
import com.leaf.common.model.RegisterMeta;
import com.leaf.common.model.ServiceMeta;
import com.leaf.common.utils.AnyThrow;
import com.leaf.common.utils.Collections;
import com.leaf.register.api.AbstractRegisterService;
import com.leaf.register.api.model.Notify;
import com.leaf.remoting.api.*;
import com.leaf.remoting.api.channel.ChannelGroup;
import com.leaf.remoting.api.future.ResponseFuture;
import com.leaf.remoting.api.payload.RequestCommand;
import com.leaf.remoting.api.payload.ResponseCommand;
import com.leaf.remoting.netty.NettyClient;
import com.leaf.remoting.netty.NettyClientConfig;
import com.leaf.serialization.api.Serializer;
import com.leaf.serialization.api.SerializerFactory;
import com.leaf.serialization.api.SerializerType;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import io.netty.util.internal.SystemPropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimerTask;
import java.util.concurrent.*;

import static com.leaf.common.ProtocolHead.ACK;
import static com.leaf.common.ProtocolHead.SUBSCRIBE_RECEIVE;

public class DefaultRegisterClient {

    private static final Logger logger = LoggerFactory.getLogger(DefaultRegisterClient.class);

    private static final AttributeKey<ConcurrentSet<RegisterMeta>> REGISTER_KEY = AttributeKey.valueOf("register.key");

    private static final AttributeKey<ConcurrentSet<ServiceMeta>> SUBSCRIBE_KEY = AttributeKey.valueOf("subscribe.key");
    private static final SerializerType serializerType;

    static {
        serializerType = SerializerType.parse(
                (byte) SystemPropertyUtil.getInt("serializer.serializerType", SerializerType.PROTO_STUFF.value()));
    }

    private final RpcClient rpcClient;
    private final NettyClientConfig config = new NettyClientConfig();
    private final AbstractRegisterService registerService;
    private final ConcurrentHashMap<Long, ResendMessage> resendMessages = new ConcurrentHashMap<>();
    private final ScheduledExecutorService resendMessageTimer;
    private volatile Channel channel;

    public DefaultRegisterClient(AbstractRegisterService registerService) {
        this.registerService = registerService;
        this.rpcClient = new NettyClient(config, new RegisterClientChannelEventProcess());
        this.rpcClient.start();

        this.resendMessageTimer = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("resend-message-timer");
                return thread;
            }
        });
        this.resendMessageTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (ResendMessage resendMessage : resendMessages.values()) {
                    if (System.currentTimeMillis() - resendMessage.getTimestamp() > 100) {
                        try {
                            rpcClient.invokeAsync(
                                    channel,
                                    resendMessage.getRequestCommand(),
                                    config.getInvokeTimeoutMillis(),
                                    resendMessage.getRegisterInvokeCallback()
                            );
                        } catch (Exception e) {
                            logger.error("resend no ack message error! {}", e.getMessage(), e);
                        }
                    }
                }
            }
        }, 1000, Constants.DEFAULT_RESNED_INTERVAL, TimeUnit.MILLISECONDS);
    }

    public void connect(UnresolvedAddress unresolvedAddress) {
        try {
            rpcClient.connect(unresolvedAddress);
            rpcClient.registerRequestProcess(new RegisterClientProcess(), Executors.newCachedThreadPool());
            ChannelGroup group = rpcClient.group(unresolvedAddress);
            this.channel = group.next();
        } catch (Exception e) {
            AnyThrow.throwUnchecked(e);
        }
    }

    private void sendMessageToRegister(RequestCommand requestCommand) {
        RegisterInvokeCallback registerInvokeCallback = new RegisterInvokeCallback();
        try {
            // 重连之后 由于异步事件，可能导致channel没有及时被赋值成重连后的channel
            if (!channel.isActive()) {
                TimeUnit.SECONDS.sleep(1);
            }
            // 如果 channel 还是 isActive == false , 发送失败异常，会重发消息
            rpcClient.invokeAsync(
                    channel,
                    requestCommand,
                    config.getInvokeTimeoutMillis(),
                    registerInvokeCallback
            );
        } catch (Exception e) {
            logger.error("send message to register error! {}", e.getMessage(), e);
        } finally {
            // 悲观策略 默认失败注册失败，重新注册 直到收到成功ACK
            ResendMessage resendMessage = new ResendMessage(requestCommand, registerInvokeCallback);
            resendMessages.put(requestCommand.getInvokeId(), resendMessage);
        }
    }

    public void register(RegisterMeta registerMeta) {
        if (attachRegisterEvent(registerMeta, channel)) {
            Serializer serializer = SerializerFactory.serializer(serializerType);
            RequestCommand requestCommand = RemotingCommandFactory.createRequestCommand(
                    ProtocolHead.REGISTER_SERVICE,
                    serializerType.value(),
                    serializer.serialize(registerMeta));

            sendMessageToRegister(requestCommand);
        }
    }

    public void unRegister(RegisterMeta registerMeta) {
        if (attachCancelRegisterEvent(registerMeta, channel)) {
            Serializer serializer = SerializerFactory.serializer(serializerType);
            RequestCommand requestCommand = RemotingCommandFactory.createRequestCommand(
                    ProtocolHead.CANCEL_REGISTER_SERVICE,
                    serializerType.value(),
                    serializer.serialize(registerMeta));
            sendMessageToRegister(requestCommand);
        }
    }


    public void subscribe(ServiceMeta serviceMeta) {
        if (attachSubscribeEvent(serviceMeta, channel)) {
            Serializer serializer = SerializerFactory.serializer(serializerType);
            RequestCommand requestCommand = RemotingCommandFactory.createRequestCommand(
                    ProtocolHead.SUBSCRIBE_SERVICE,
                    serializerType.value(),
                    serializer.serialize(serviceMeta));
            sendMessageToRegister(requestCommand);
        }
    }

    // channel 附着注册的服务，忽略重复注册
    private boolean attachRegisterEvent(RegisterMeta registerMeta, Channel channel) {
        ConcurrentSet<RegisterMeta> registerMetas = channel.attr(REGISTER_KEY).get();
        if (registerMetas == null) {
            ConcurrentSet<RegisterMeta> newRegisterMetas = new ConcurrentSet<>();
            registerMetas = channel.attr(REGISTER_KEY).setIfAbsent(newRegisterMetas);
            if (registerMetas == null) {
                registerMetas = newRegisterMetas;
            }
        }
        return registerMetas.add(registerMeta);
    }

    private boolean attachCancelRegisterEvent(RegisterMeta registerMeta, Channel channel) {
        ConcurrentSet<RegisterMeta> registerMetas = channel.attr(REGISTER_KEY).get();
        if (registerMetas == null) {
            return false;
        }
        return registerMetas.remove(registerMeta);
    }

    // channel 附着订阅的服务，忽略重复订阅
    private boolean attachSubscribeEvent(ServiceMeta serviceMeta, Channel channel) {
        ConcurrentSet<ServiceMeta> serviceMetas = channel.attr(SUBSCRIBE_KEY).get();
        if (serviceMetas == null) {
            ConcurrentSet<ServiceMeta> newServiceMetas = new ConcurrentSet<>();
            serviceMetas = channel.attr(SUBSCRIBE_KEY).setIfAbsent(newServiceMetas);
            if (serviceMetas == null) {
                serviceMetas = newServiceMetas;
            }
        }
        return serviceMetas.add(serviceMeta);
    }

    class RegisterClientChannelEventProcess extends ChannelEventAdapter {

        @Override
        public void onChannelActive(String remoteAddr, Channel channel) {
            DefaultRegisterClient.this.channel = channel;
            // 重新连接 重新发布 订阅服务
            ConcurrentSet<RegisterMeta> providers = registerService.getProviderRegisterMetas();
            if (Collections.isNotEmpty(providers)) {
                for (RegisterMeta registerMeta : providers) {
                    register(registerMeta);
                }
            }
            ConcurrentSet<ServiceMeta> consumers = registerService.getConsumersServiceMeta();
            if (Collections.isEmpty(consumers)) {
                for (ServiceMeta serviceMeta : consumers) {
                    subscribe(serviceMeta);
                }
            }
        }
    }

    class RegisterClientProcess implements RequestProcessor {

        @Override
        public ResponseCommand process(ChannelHandlerContext context, RequestCommand request) {
            Serializer serializer = SerializerFactory.serializer(SerializerType.parse(request.getSerializerCode()));
            Notify notifyData = serializer.deserialize(request.getBody(), Notify.class);
            switch (request.getMessageCode()) {
                case ProtocolHead.SUBSCRIBE_SERVICE: {
                    if (Collections.isEmpty(notifyData.getRegisterMetas())) {
                        throw new IllegalStateException("[SUBSCRIBE]" + notifyData.getServiceMeta() + " no provider!");
                    }
                    registerService.notify(notifyData.getServiceMeta(),
                            notifyData.getEvent(),
                            notifyData.getRegisterMetas());
                    break;
                }
                case ProtocolHead.OFFLINE_SERVICE: {
                    UnresolvedAddress address = notifyData.getAddress();
                    registerService.offline(address);
                    break;
                }
                default:
                    throw new UnsupportedOperationException("RegisterClientProcess Unsupported MessageCode: " + request.getMessageCode());
            }
            ResponseCommand responseCommand = RemotingCommandFactory.createResponseCommand(
                    ProtocolHead.ACK,
                    serializerType.value(),
                    null,
                    request.getInvokeId()
            );
            return responseCommand;
        }

        @Override
        public boolean rejectRequest() {
            return false;
        }
    }

    class RegisterInvokeCallback implements InvokeCallback<ResponseCommand> {

        public RegisterInvokeCallback() {
        }

        @Override
        public void operationComplete(ResponseFuture<ResponseCommand> responseFuture) {
            Serializer serializer = SerializerFactory.serializer(serializerType);
            ResponseCommand responseCommand = responseFuture.result();
            if (responseCommand == null) {
                // 通常是客户端异常 或 等待服务端超时
                Throwable cause = responseFuture.cause();
                if (cause != null) {
                    logger.error(cause.getMessage(), cause);
                } else {
                    logger.warn("Not only not received any message from provider, but cause is null!");
                }
            } else {
                // 收到ack确认，删除重发消息
                if (responseCommand.getMessageCode() == ACK) {
                    resendMessages.remove(responseCommand.getInvokeId());
                }
                if (responseCommand.getMessageCode() == SUBSCRIBE_RECEIVE) {
                    if (responseCommand.getStatus() == ResponseStatus.SUCCESS.value()) {
                        resendMessages.remove(responseCommand.getInvokeId());

                        Notify notifyData = serializer.deserialize(responseCommand.getBody(), Notify.class);
                        if (Collections.isEmpty(notifyData.getRegisterMetas())) {
                            throw new IllegalStateException("[SUBSCRIBE] " + notifyData.getServiceMeta() + " no provider!");
                        }
                        registerService.notify(notifyData.getServiceMeta(),
                                notifyData.getEvent(),
                                notifyData.getRegisterMetas());
                    } else {
                        logger.warn("[SUBSCRIBE] receive register message, but response status: {}",
                                responseCommand.getMessageCode());
                    }
                }
            }
        }
    }

    class ResendMessage {

        private RequestCommand requestCommand;

        private InvokeCallback<ResponseCommand> registerInvokeCallback;

        private long timestamp;

        public ResendMessage(RequestCommand requestCommand, InvokeCallback<ResponseCommand> registerInvokeCallback) {
            this.requestCommand = requestCommand;
            this.registerInvokeCallback = registerInvokeCallback;
            this.timestamp = System.currentTimeMillis();
        }

        public RequestCommand getRequestCommand() {
            return requestCommand;
        }

        public InvokeCallback<ResponseCommand> getRegisterInvokeCallback() {
            return registerInvokeCallback;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

}
