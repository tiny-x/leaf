package com.leaf.rpc.provider.process;

import com.leaf.common.ProtocolHead;
import com.leaf.common.model.RequestWrapper;
import com.leaf.common.model.ResponseWrapper;
import com.leaf.common.model.ServiceWrapper;
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

    /**
     * logger
     */
    private final static Logger logger = LoggerFactory.getLogger(DefaultProviderProcessor.class);

    private final Provider provider;

    public DefaultProviderProcessor(Provider provider) {
        this.provider = provider;
    }

    @Override
    public ResponseCommand process(ChannelHandlerContext context, RequestCommand request) {
        Serializer serializer = SerializerFactory.serializer(SerializerType.parse(request.getSerializerCode()));
        switch (request.getMessageCode()) {
            case ProtocolHead.REQUEST: {
                RequestWrapper requestWrapper = serializer.deserialize(request.getBody(), RequestWrapper.class);
                ServiceWrapper serviceWrapper = provider.lookupService(requestWrapper.getServiceMeta());

                ResponseWrapper responseWrapper = new ResponseWrapper();
                ResponseCommand responseCommand = null;
                if (serviceWrapper == null) {

                    String message = String.format(
                            "%s service: [%s] not found",
                            context.channel(),
                            requestWrapper.getServiceMeta()
                    );
                    logger.warn(message);
                    responseWrapper.setResult(message);
                } else {
                    Object result = Reflects.Invoke(
                            serviceWrapper.getServiceProvider(),
                            requestWrapper.getMethodName(),
                            requestWrapper.getArgs()
                    );
                    responseWrapper.setResult(result);
                }

                if (!request.isOneWay()) {
                    responseCommand = RemotingCommandFactory.createResponseCommand(
                            request.getSerializerCode(),
                            serializer.serialize(responseWrapper),
                            request.getInvokeId()
                    );
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