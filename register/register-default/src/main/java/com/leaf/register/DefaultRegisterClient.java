package com.leaf.register;

import com.leaf.common.ProtocolHead;
import com.leaf.common.UnresolvedAddress;
import com.leaf.common.concurrent.ConcurrentSet;
import com.leaf.common.model.RegisterMeta;
import com.leaf.common.model.ServiceMeta;
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

import java.util.concurrent.Executors;

public class DefaultRegisterClient {

    private static final Logger logger = LoggerFactory.getLogger(DefaultRegisterClient.class);

    private RpcClient rpcClient;

    private NettyClientConfig config = new NettyClientConfig();

    private AbstractRegisterService registerService;

    private AttributeKey<ConcurrentSet<RegisterMeta>> REGISTER_KEY = AttributeKey.valueOf("register.key");

    private AttributeKey<ConcurrentSet<ServiceMeta>> SUBSCRIBE_KEY = AttributeKey.valueOf("subscribe.key");

    private volatile Channel channel;

    private static final SerializerType serializerType;

    static {
        serializerType = SerializerType.parse(
                (byte) SystemPropertyUtil.getInt("serializer.serializerType", SerializerType.PROTO_STUFF.value()));
    }

    public DefaultRegisterClient(UnresolvedAddress unresolvedAddress, AbstractRegisterService registerService) {
        this.registerService = registerService;
        this.rpcClient = new NettyClient(config, new RegisterClientChannelEventProcess());
        rpcClient.start();
        try {
            rpcClient.connect(unresolvedAddress);
            rpcClient.registerRequestProcess(new RegisterClientProcess(), Executors.newCachedThreadPool());
            ChannelGroup group = rpcClient.group(unresolvedAddress);
            this.channel = group.next();
        } catch (Exception e) {
            logger.error("connect register fail", e);
            throw new RuntimeException(e);
        }
    }

    public void register(RegisterMeta registerMeta) {
        if (attachRegisterEvent(registerMeta, channel)) {
            Serializer serializer = SerializerFactory.serializer(serializerType);
            RequestCommand requestCommand = new RequestCommand(ProtocolHead.REGISTER_SERVICE,
                    serializerType.value(),
                    serializer.serialize(registerMeta));

            try {
                rpcClient.invokeAsync(
                        channel,
                        requestCommand,
                        config.getInvokeTimeoutMillis(),
                        new RegisterInvokeCallback(channel, requestCommand, config.getInvokeTimeoutMillis())
                );
            } catch (Exception e) {
                logger.error("register service fail", e);
            } finally {
                // 悲观策略 默认失败注册失败，重新注册 直到收到成功ACK
                // TODO 添加到重发队列
            }
        }
    }

    public void unRegister(RegisterMeta registerMeta) {
        try {
            if (attachCancelRegisterEvent(registerMeta, channel)) {
                Serializer serializer = SerializerFactory.serializer(serializerType);
                RequestCommand requestCommand = new RequestCommand(ProtocolHead.CANCEL_REGISTER_SERVICE,
                        serializerType.value(),
                        serializer.serialize(registerMeta));
                rpcClient.invokeSync(channel, requestCommand, config.getInvokeTimeoutMillis());
            }
        } catch (Exception e) {
            logger.error("unRegister service fail", e);
        }
    }


    public void subscribe(ServiceMeta serviceMeta) {
        try {
            if (attachSubscribeEvent(serviceMeta, channel)) {
                Serializer serializer = SerializerFactory.serializer(serializerType);

                RequestCommand requestCommand = new RequestCommand(ProtocolHead.SUBSCRIBE_SERVICE,
                        serializerType.value(),
                        serializer.serialize(serviceMeta));

                ResponseCommand responseCommand = rpcClient.invokeSync(channel, requestCommand, config.getInvokeTimeoutMillis());
                Notify notifyData = serializer.deserialize(responseCommand.getBody(), Notify.class);
                if (notifyData.getRegisterMetas() == null || notifyData.getRegisterMetas().size() == 0) {
                    throw new IllegalStateException("[SUBSCRIBE] " + notifyData.getServiceMeta() + " no provider!");
                }
                registerService.notify(notifyData.getServiceMeta(),
                        notifyData.getEvent(),
                        notifyData.getRegisterMetas());
            }
        } catch (Exception e) {
            logger.error("subscribe service fail", e);
        }
    }

    class RegisterClientChannelEventProcess extends ChannelEventAdapter {

        @Override
        public void onChannelActive(String remoteAddr, Channel channel) {
            DefaultRegisterClient.this.channel = channel;
            // 重新连接 重新发布 订阅服务
            ConcurrentSet<RegisterMeta> providers = registerService.getProviderRegisterMetas();
            if (providers != null && providers.size() > 0) {
                for (RegisterMeta registerMeta : providers) {
                    register(registerMeta);
                }
            }
            ConcurrentSet<ServiceMeta> consumers = registerService.getConsumersServiceMeta();
            if (consumers != null && consumers.size() > 0) {
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
                    if (notifyData.getRegisterMetas() == null || notifyData.getRegisterMetas().size() == 0) {
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
            // TODO 回复ack
            return null;
        }

        @Override
        public boolean rejectRequest() {
            return false;
        }
    }

    class RegisterInvokeCallback implements InvokeCallback<ResponseCommand> {

        private Channel channel;

        private RequestCommand requestCommand;

        private long invokeTimeoutMillis;

        public RegisterInvokeCallback(Channel channel, RequestCommand requestCommand, long invokeTimeoutMillis) {
            this.channel = channel;
            this.requestCommand = requestCommand;
            this.invokeTimeoutMillis = invokeTimeoutMillis;
        }

        @Override
        public void operationComplete(ResponseFuture<ResponseCommand> responseFuture) {

            ResponseCommand responseCommand = responseFuture.result();
            if (responseCommand.getStatus() != ResponseStatus.SUCCESS.value()) {
                // 重新发送消息
                try {
                    rpcClient.invokeAsync(
                            channel,
                            requestCommand,
                            invokeTimeoutMillis,
                            this
                    );
                } catch (Exception e) {
                    // TODO

                }
            } else {

            }
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
}
