package com.leaf.register.process;

import com.leaf.common.ProtocolHead;
import com.leaf.common.UnresolvedAddress;
import com.leaf.common.concurrent.ConcurrentSet;
import com.leaf.common.constants.Constants;
import com.leaf.register.api.model.RegisterMeta;
import com.leaf.common.model.ServiceMeta;
import com.leaf.common.utils.Collections;
import com.leaf.common.utils.Maps;
import com.leaf.register.DefaultRegisterServer;
import com.leaf.register.api.NotifyEvent;
import com.leaf.register.api.model.Notify;
import com.leaf.remoting.api.ChannelEventAdapter;
import com.leaf.remoting.api.InvokeCallback;
import com.leaf.remoting.api.RemotingCommandFactory;
import com.leaf.remoting.api.RequestProcessor;
import com.leaf.remoting.api.future.ResponseFuture;
import com.leaf.remoting.api.payload.RequestCommand;
import com.leaf.remoting.api.payload.ResponseCommand;
import com.leaf.serialization.api.Serializer;
import com.leaf.serialization.api.SerializerFactory;
import com.leaf.serialization.api.SerializerType;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.SystemPropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.*;

import static com.leaf.common.ProtocolHead.*;

public class RegisterProcess implements RequestProcessor {

    private static final Logger logger = LoggerFactory.getLogger(RegisterProcess.class);
    private static final SerializerType serializerType;

    static {
        serializerType = SerializerType.parse(
                (byte) SystemPropertyUtil.getInt("serializer.serializerType", SerializerType.PROTO_STUFF.value()));
    }

    private final ConcurrentMap<String, ConcurrentSet<ServiceMeta>> CONSUMER_MAP = Maps.newConcurrentMap();
    private final ConcurrentMap<String, ConcurrentSet<RegisterMeta>> PROVIDER_MAP = Maps.newConcurrentMap();
    private final AttributeKey<ConcurrentSet<ServiceMeta>> SUBSCRIBE_KEY = AttributeKey.valueOf("server.subscribed");
    private final AttributeKey<ConcurrentSet<RegisterMeta>> PUBLISH_KEY = AttributeKey.valueOf("server.publish");
    private final DefaultRegisterServer defaultRegisterServer;
    // 订阅者
    private final ChannelGroup subscriberChannels =
            new DefaultChannelGroup("subscribers", GlobalEventExecutor.INSTANCE);
    private final ConcurrentMap<Long, ResendMessage> resendMessages = Maps.newConcurrentMap();

    private final ScheduledExecutorService resendMessageTimer;

    public RegisterProcess(DefaultRegisterServer defaultRegisterServer) {
        this.defaultRegisterServer = defaultRegisterServer;

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
                            defaultRegisterServer.getRpcServer().invokeAsync(
                                    resendMessage.getChannel(),
                                    resendMessage.getRequestCommand(),
                                    defaultRegisterServer.getNettyServerConfig().getInvokeTimeoutMillis(),
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

    @Override
    public ResponseCommand process(ChannelHandlerContext context, RequestCommand request) {
        Serializer serializer = SerializerFactory.serializer(SerializerType.parse(request.getSerializerCode()));

        switch (request.getMessageCode()) {
            case REGISTER_SERVICE: {
                return handleRegisterService(context, request, serializer);
            }
            case SUBSCRIBE_SERVICE: {
                return handleSubscribeService(context, request, serializer);
            }
            case CANCEL_REGISTER_SERVICE: {
                return handleUnRegisterService(request, serializer);
            }
            case LOOKUP_SERVICE: {
                return handleLookupService(request, serializer);
            }
            default:
                throw new UnsupportedOperationException("RegisterProcess Unsupported MessageCode: " + request.getMessageCode());
        }
    }

    // 查找服务
    private ResponseCommand handleLookupService(RequestCommand request, Serializer serializer) {
        String serviceName = serializer.deserialize(request.getBody(), String.class);

        logger.info("[SUBSCRIBE] subscribe service: {}", serviceName);

        ConcurrentSet<RegisterMeta> registerMetas = PROVIDER_MAP.get(serviceName);
        List<RegisterMeta> registerMetasList = new ArrayList<>(registerMetas);

        // 返回给客户端已经注册的服务
        ResponseCommand responseCommand = RemotingCommandFactory.createResponseCommand(
                ProtocolHead.ACK,
                request.getSerializerCode(),
                serializer.serialize(null),
                request.getInvokeId()
        );
        return responseCommand;
    }

    // 订阅服务
    private ResponseCommand handleSubscribeService(ChannelHandlerContext context, RequestCommand request, Serializer serializer) {
        ServiceMeta serviceMeta = serializer.deserialize(request.getBody(), ServiceMeta.class);

        logger.info("[SUBSCRIBE] subscribe service: {}", serviceMeta);

        // channel 附上 订阅的服务（三元素）
        Channel channel = context.channel();
        attachSubscribeEvent(serviceMeta, channel);

        String serviceProviderName = serviceMeta.getServiceProviderName();
        ConcurrentSet<ServiceMeta> consumers = CONSUMER_MAP.get(serviceProviderName);
        if (consumers == null) {
            ConcurrentSet<ServiceMeta> newConsumers = new ConcurrentSet();
            consumers = CONSUMER_MAP.putIfAbsent(serviceProviderName, newConsumers);
            if (consumers == null) {
                consumers = newConsumers;
            }
        }
        consumers.add(serviceMeta);

        ConcurrentSet<RegisterMeta> providers = PROVIDER_MAP.get(serviceProviderName);
        Notify notify = new Notify(
                NotifyEvent.ADD,
                serviceMeta,
                new ArrayList<>(providers)
        );

        // 返回给客户端已经注册的服务
        ResponseCommand responseCommand = RemotingCommandFactory.createResponseCommand(
                ProtocolHead.SUBSCRIBE_RECEIVE,
                request.getSerializerCode(),
                serializer.serialize(notify),
                request.getInvokeId()
        );

        return responseCommand;
    }

    // 注册服务
    private ResponseCommand handleRegisterService(ChannelHandlerContext context, RequestCommand request, Serializer serializer) {
        RegisterMeta registerMeta = serializer.deserialize(request.getBody(), RegisterMeta.class);

        logger.info("[REGISTER] register service: {}", registerMeta);

        String serviceProviderName = registerMeta.getServiceMeta().getServiceProviderName();
        ConcurrentSet<RegisterMeta> providers = PROVIDER_MAP.get(serviceProviderName);
        if (providers == null) {
            ConcurrentSet<RegisterMeta> newProviders = new ConcurrentSet();
            providers = PROVIDER_MAP.putIfAbsent(serviceProviderName, newProviders);
            if (providers == null) {
                providers = newProviders;
            }
        }
        providers.add(registerMeta);

        // channel 绑定服务
        Channel channel = context.channel();
        attachRegisterEvent(registerMeta, channel);

        // 通知订阅者
        notifySubscribe(serializer, registerMeta, NotifyEvent.ADD);

        // 回复服务端注册成功
        ResponseCommand responseCommand = RemotingCommandFactory.createResponseCommand(
                ProtocolHead.ACK,
                request.getSerializerCode(),
                null,
                request.getInvokeId()
        );
        return responseCommand;
    }

    // 取消注册服务
    private ResponseCommand handleUnRegisterService(RequestCommand request, Serializer serializer) {
        RegisterMeta registerMeta = serializer.deserialize(request.getBody(), RegisterMeta.class);
        logger.info("[UN_REGISTER] cancel register service: {}", registerMeta);
        String serviceProviderName = registerMeta.getServiceMeta().getServiceProviderName();
        ConcurrentSet<RegisterMeta> registerMetaList = PROVIDER_MAP.get(serviceProviderName);
        if (Collections.isNotEmpty(registerMetaList)) {
            registerMetaList.remove(registerMeta);
        }

        // 通知订阅者
        notifySubscribe(serializer, registerMeta, NotifyEvent.REMOVE);

        // 回复服务端下线服务成功
        ResponseCommand responseCommand = RemotingCommandFactory.createResponseCommand(
                ProtocolHead.ACK,
                request.getSerializerCode(),
                null,
                request.getInvokeId()
        );

        return responseCommand;
    }

    @Override
    public boolean rejectRequest() {
        return false;
    }


    private void notifySubscribe(Serializer serializer, RegisterMeta registerMeta, NotifyEvent notifyEvent) {
        ArrayList<RegisterMeta> registerMetas = new ArrayList<>(1);
        registerMetas.add(registerMeta);
        Notify notify = new Notify(
                notifyEvent,
                registerMeta.getServiceMeta(),
                registerMetas
        );

        RequestCommand requestCommand = RemotingCommandFactory.createRequestCommand(
                SUBSCRIBE_SERVICE,
                serializerType.value(),
                serializer.serialize(notify)
        );

        Iterator<Channel> iterator = subscriberChannels.iterator();
        while (iterator.hasNext()) {
            Channel next = iterator.next();
            if (channelIsSubscribeService(next, registerMeta.getServiceMeta())) {
                sendMessageToSubscriber(requestCommand, next);
            }
        }
    }

    private boolean channelIsSubscribeService(Channel channel, ServiceMeta serviceMeta) {
        Attribute<ConcurrentSet<ServiceMeta>> attr = channel.attr(SUBSCRIBE_KEY);
        ConcurrentSet<ServiceMeta> serviceMetas = attr.get();
        if (Collections.isNotEmpty(serviceMetas) && serviceMetas.contains(serviceMeta)) {
            return true;
        }
        return false;
    }

    private void attachRegisterEvent(RegisterMeta registerMeta, Channel channel) {
        ConcurrentSet<RegisterMeta> services = channel.attr(PUBLISH_KEY).get();
        if (services == null) {
            ConcurrentSet<RegisterMeta> newServices = new ConcurrentSet<>();
            services = channel.attr(PUBLISH_KEY).setIfAbsent(newServices);
            if (services == null) {
                services = newServices;
            }
        }
        services.add(registerMeta);
    }

    private void attachOfflineEvent(RegisterMeta registerMeta, Channel channel) {
        ConcurrentSet<RegisterMeta> services = channel.attr(PUBLISH_KEY).get();
        if (services == null) {
            ConcurrentSet<RegisterMeta> newServices = new ConcurrentSet<>();
            services = channel.attr(PUBLISH_KEY).setIfAbsent(newServices);
            if (services == null) {
                services = newServices;
            }
        }
        services.remove(registerMeta);
    }

    private void attachSubscribeEvent(ServiceMeta serviceMeta, Channel channel) {
        Attribute<ConcurrentSet<ServiceMeta>> attr = channel.attr(SUBSCRIBE_KEY);
        ConcurrentSet<ServiceMeta> serviceMetas = attr.get();
        if (serviceMetas == null) {
            ConcurrentSet<ServiceMeta> newServiceMetas = new ConcurrentSet<>();
            serviceMetas = attr.setIfAbsent(newServiceMetas);
            if (serviceMetas == null) {
                serviceMetas = newServiceMetas;
            }
        }
        serviceMetas.add(serviceMeta);
        subscriberChannels.add(channel);
    }

    private void sendMessageToSubscriber(RequestCommand requestCommand, Channel channel) {
        RegisterServerInvokeCallback registerServerInvokeCallback = new RegisterServerInvokeCallback();
        try {
            defaultRegisterServer.getRpcServer().invokeAsync(
                    channel,
                    requestCommand,
                    defaultRegisterServer.getNettyServerConfig().getInvokeTimeoutMillis(),
                    registerServerInvokeCallback

            );
        } catch (Exception e) {
            logger.error("send message to subscriber error! {}, channel: {}", e.getMessage(), channel, e);
        } finally {
            // 悲观策略 默认失败注册失败，重新注册 直到收到成功ACK
            ResendMessage resendMessage = new ResendMessage(channel, requestCommand, registerServerInvokeCallback);
            resendMessages.put(requestCommand.getInvokeId(), resendMessage);
        }
    }

    public class RegisterChannelEventProcess extends ChannelEventAdapter {

        @Override
        public void onChannelInActive(String remoteAddr, Channel channel) {
            ConcurrentSet<RegisterMeta> registerMetas = channel.attr(PUBLISH_KEY).get();
            if (Collections.isNotEmpty(registerMetas)) {
                logger.info("[OFFLINE_SERVICE] server: {} offline", remoteAddr);

                UnresolvedAddress address = null;
                for (RegisterMeta registerMeta : registerMetas) {
                    address = registerMeta.getAddress();
                    attachOfflineEvent(registerMeta, channel);
                    ConcurrentSet<RegisterMeta> services = PROVIDER_MAP.get(registerMeta.getServiceMeta().getServiceProviderName());
                    services.remove(registerMeta);
                }

                // 通知订阅下线服务
                Notify notify = new Notify(address);
                Serializer serializer = SerializerFactory.serializer(serializerType);
                RequestCommand requestCommand = RemotingCommandFactory.createRequestCommand(
                        OFFLINE_SERVICE,
                        serializerType.value(),
                        serializer.serialize(notify)
                );

                Iterator<Channel> iterator = subscriberChannels.iterator();
                while (iterator.hasNext()) {
                    Channel next = iterator.next();
                    sendMessageToSubscriber(requestCommand, next);
                }
            }
        }
    }

    class RegisterServerInvokeCallback implements InvokeCallback<ResponseCommand> {

        public RegisterServerInvokeCallback() {
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
            }
        }
    }

    class ResendMessage {

        private Channel channel;

        private RequestCommand requestCommand;

        private InvokeCallback<ResponseCommand> registerInvokeCallback;

        private long timestamp;

        public ResendMessage(Channel channel, RequestCommand requestCommand,
                             InvokeCallback<ResponseCommand> registerInvokeCallback) {

            this.requestCommand = requestCommand;
            this.channel = channel;
            this.registerInvokeCallback = registerInvokeCallback;
            this.timestamp = System.currentTimeMillis();
        }

        public RequestCommand getRequestCommand() {
            return requestCommand;
        }

        public InvokeCallback<ResponseCommand> getRegisterInvokeCallback() {
            return registerInvokeCallback;
        }

        public Channel getChannel() {
            return channel;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    public void shutdownGracefully() {
        try {
            if (resendMessageTimer != null) {
                resendMessageTimer.shutdown();
            }
        } catch (Exception e) {
            logger.error("register shutdown gracefully error!", e);
        }
    }

}