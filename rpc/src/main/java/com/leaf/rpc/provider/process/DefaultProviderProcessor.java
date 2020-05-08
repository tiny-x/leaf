package com.leaf.rpc.provider.process;

import com.leaf.common.context.RpcContext;
import com.leaf.remoting.api.ProtocolHead;
import com.leaf.remoting.api.RequestWrapper;
import com.leaf.remoting.api.ResponseWrapper;
import com.leaf.rpc.local.ServiceWrapper;
import com.leaf.common.utils.Reflects;
import com.leaf.remoting.api.RemotingCommandFactory;
import com.leaf.remoting.api.RequestProcessor;
import com.leaf.remoting.api.ResponseStatus;
import com.leaf.remoting.api.payload.RequestCommand;
import com.leaf.remoting.api.payload.ResponseCommand;
import com.leaf.rpc.controller.FlowController;
import com.leaf.rpc.provider.Provider;
import com.leaf.serialization.api.Serializer;
import com.leaf.serialization.api.SerializerFactory;
import com.leaf.serialization.api.SerializerType;
import io.netty.channel.ChannelHandlerContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionException;

public class DefaultProviderProcessor implements RequestProcessor {

    private final static Logger logger = LoggerFactory.getLogger(DefaultProviderProcessor.class);

    private final Provider provider;

    public DefaultProviderProcessor(Provider provider) {
        this.provider = provider;
    }

    @Override
    public ResponseCommand process(ChannelHandlerContext context, RequestCommand request) {
        Serializer serializer = SerializerFactory.serializer(SerializerType.parse(request.getSerializerCode()));
        switch (request.getMessageCode()) {
            case ProtocolHead.RPC_REQUEST: {
            }
            case ProtocolHead.ONEWAY_REQUEST: {
                RequestWrapper requestWrapper = serializer.deserialize(request.getBody(), RequestWrapper.class);
                ServiceWrapper serviceWrapper = provider.lookupService(requestWrapper.getServiceMeta());

                Object result = null;
                if (serviceWrapper == null) {
                    String message = String.format(
                            "%s service: [%s] not found",
                            context.channel(),
                            requestWrapper.getServiceMeta()
                    );
                    logger.error(message);
                } else {
                    try {
                        RpcContext.setAttachments(requestWrapper.getAttachment());
                        result = Reflects.Invoke(
                                serviceWrapper.getServiceProvider(),
                                requestWrapper.getMethodName(),
                                requestWrapper.getArgs()
                        );
                        RpcContext.clearAttachments();
                    } catch (Throwable t) {
                        logger.error(t.getMessage(), t);
                        result = t;
                    }
                }

                ResponseCommand responseCommand = null;
                if (!request.isOneWay()) {
                    ResponseWrapper responseWrapper = new ResponseWrapper();
                    if (result instanceof Throwable) {
                        responseWrapper.setCase((Throwable) result);
                    } else {
                        responseWrapper.setResult(result);
                    }

                    responseCommand = RemotingCommandFactory.createResponseCommand(
                            request.getSerializerCode(),
                            serializer.serialize(responseWrapper),
                            request.getInvokeId()
                    );

                    if (result instanceof Throwable) {
                        responseCommand.setStatus(ResponseStatus.SERVER_ERROR.value());
                    }
                    if (serviceWrapper == null) {
                        responseCommand.setStatus(ResponseStatus.SERVICE_NOT_FOUND.value());
                    }
                }
                return responseCommand;
            }
            default: {
                String errorMessage = String.format("DefaultProviderProcessor Unsupported MessageCode: %d",
                        request.getMessageCode());
                throw new UnsupportedOperationException(errorMessage);
            }
        }
    }

    @Override
    public boolean rejectRequest() {
        FlowController[] flowControllers = provider.globalFlowController();
        if (flowControllers != null && flowControllers.length > 0) {
            for (FlowController flowController : flowControllers) {
                try {
                    flowController.flowController();
                } catch (RejectedExecutionException e) {
                    logger.error(e.getMessage(), e);
                    return true;
                }
            }
        }
        return false;
    }
}