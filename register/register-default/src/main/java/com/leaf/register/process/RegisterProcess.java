package com.leaf.register.process;

import com.leaf.common.ProtocolHead;
import com.leaf.common.UnresolvedAddress;
import com.leaf.common.concurrent.ConcurrentSet;
import com.leaf.common.model.RegisterMeta;
import com.leaf.common.model.ServiceMeta;
import com.leaf.register.api.NotifyEvent;
import com.leaf.register.api.model.Notify;
import com.leaf.register.api.model.RegisterMetas;
import com.leaf.remoting.api.ChannelEventAdapter;
import com.leaf.remoting.api.RemotingCommandFactory;
import com.leaf.remoting.api.RequestProcessor;
import com.leaf.remoting.api.payload.RequestCommand;
import com.leaf.remoting.api.payload.ResponseCommand;
import com.leaf.serialization.api.Serializer;
import com.leaf.serialization.api.SerializerFactory;
import com.leaf.serialization.api.SerializerType;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelMatcher;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.SystemPropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.leaf.common.ProtocolHead.*;

public class RegisterProcess implements RequestProcessor {

    private static final Logger logger = LoggerFactory.getLogger(RegisterProcess.class);

    private static final ConcurrentMap<String, ConcurrentSet<ServiceMeta>> CONSUMER_MAP = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, ConcurrentSet<RegisterMeta>> PROVIDER_MAP = new ConcurrentHashMap<>();

    private static final AttributeKey<ConcurrentSet<ServiceMeta>> SUBSCRIBE_KEY =
            AttributeKey.valueOf("server.subscribed");

    private static final AttributeKey<ConcurrentSet<RegisterMeta>> PUBLISH_KEY =
            AttributeKey.valueOf("server.publish");

    private static final SerializerType serializerType;

    // 订阅者
    private static final ChannelGroup subscriberChannels =
            new DefaultChannelGroup("subscribers", GlobalEventExecutor.INSTANCE);

    static {
        serializerType = SerializerType.parse(
                (byte) SystemPropertyUtil.getInt("serializer.serializerType", SerializerType.PROTO_STUFF.value()));
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
                String serviceName = serializer.deserialize(request.getBody(), String.class);
                ConcurrentSet<RegisterMeta> registerMetas = PROVIDER_MAP.get(serviceName);
                List<RegisterMeta> registerMetasList = new ArrayList<>(registerMetas);

                RegisterMetas result = new RegisterMetas();
                result.setRegisterMetas(registerMetasList);

                // 返回给客户端已经注册的服务
                ResponseCommand responseCommand = RemotingCommandFactory.createResponseCommand(
                        ProtocolHead.ACK,
                        request.getSerializerCode(),
                        serializer.serialize(result),
                        request.getInvokeId()
                );
                return responseCommand;
            }
            default:
                throw new UnsupportedOperationException("RegisterProcess Unsupported MessageCode: " + request.getMessageCode());
        }
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
                ProtocolHead.ACK,
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
        ArrayList<RegisterMeta> registerMetas = new ArrayList<>(1);
        registerMetas.add(registerMeta);
        Notify notify = new Notify(
                NotifyEvent.ADD,
                registerMeta.getServiceMeta(),
                registerMetas
        );

        RequestCommand requestCommand = RemotingCommandFactory.createRequestCommand(
                SUBSCRIBE_SERVICE,
                serializerType.value(),
                serializer.serialize(notify)
        );

        subscriberChannels.writeAndFlush(requestCommand, new ChannelMatcher() {
            @Override
            public boolean matches(Channel channel) {
                Attribute<ConcurrentSet<ServiceMeta>> attr = channel.attr(SUBSCRIBE_KEY);
                ConcurrentSet<ServiceMeta> serviceMetas = attr.get();
                return serviceMetas != null && serviceMetas.contains(registerMeta.getServiceMeta());
            }
        });

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
        if (registerMetaList != null && registerMetaList.size() > 0) {
            registerMetaList.remove(registerMeta);
        }

        // 通知订阅者
        ArrayList<RegisterMeta> registerMetas = new ArrayList<>(1);
        registerMetas.add(registerMeta);
        Notify notify = new Notify(
                NotifyEvent.REMOVE,
                registerMeta.getServiceMeta(),
                registerMetas
        );

        RequestCommand requestCommand = RemotingCommandFactory.createRequestCommand(
                SUBSCRIBE_SERVICE,
                serializerType.value(),
                serializer.serialize(notify)
        );

        subscriberChannels.writeAndFlush(requestCommand, new ChannelMatcher() {
            @Override
            public boolean matches(Channel channel) {
                Attribute<ConcurrentSet<ServiceMeta>> attr = channel.attr(SUBSCRIBE_KEY);
                ConcurrentSet<ServiceMeta> serviceMetas = attr.get();
                return serviceMetas != null && serviceMetas.contains(registerMeta.getServiceMeta());
            }
        });

        // 回复服务端下线服务成功
        ResponseCommand responseCommand = RemotingCommandFactory.createResponseCommand(
                ProtocolHead.ACK,
                request.getSerializerCode(),
                serializer.serialize(notify),
                request.getInvokeId()
        );

        return responseCommand;
    }

    @Override
    public boolean rejectRequest() {
        return false;
    }


    private static void attachRegisterEvent(RegisterMeta registerMeta, Channel channel) {
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

    private static void attachOfflineEvent(RegisterMeta registerMeta, Channel channel) {
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

    private static void attachSubscribeEvent(ServiceMeta serviceMeta, Channel channel) {
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

    public class RegisterChannelEventProcess extends ChannelEventAdapter {

        @Override
        public void onChannelInActive(String remoteAddr, Channel channel) {
            ConcurrentSet<RegisterMeta> registerMetas = channel.attr(PUBLISH_KEY).get();
            if (registerMetas != null && registerMetas.size() > 0) {
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
                subscriberChannels.writeAndFlush(requestCommand);
            }
        }
    }
}