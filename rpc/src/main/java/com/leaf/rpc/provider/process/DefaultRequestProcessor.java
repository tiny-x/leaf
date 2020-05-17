package com.leaf.rpc.provider.process;

import com.leaf.common.context.RpcContext;
import com.leaf.common.utils.Reflects;
import com.leaf.remoting.api.ProtocolHead;
import com.leaf.remoting.api.RemotingCommandFactory;
import com.leaf.remoting.api.RequestCommandProcessor;
import com.leaf.remoting.api.ResponseStatus;
import com.leaf.remoting.api.payload.RequestCommand;
import com.leaf.remoting.api.payload.ResponseCommand;
import com.leaf.rpc.container.ServiceProviderContainer;
import com.leaf.rpc.controller.FlowController;
import com.leaf.rpc.local.ServiceWrapper;
import com.leaf.serialization.api.Serializer;
import com.leaf.serialization.api.SerializerFactory;
import com.leaf.serialization.api.SerializerType;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RejectedExecutionException;

public class DefaultRequestProcessor implements RequestProcessor {

    private final static Logger logger = LoggerFactory.getLogger(DefaultRequestProcessor.class);

    private final ServiceProviderContainer serviceProviderContainer;

    private final CopyOnWriteArrayList<RequestProcessFilter> filters = new CopyOnWriteArrayList<>();

    private RequestCommandProcessor requestCommandProcessor;

    private FlowController[] flowControllers;

    public DefaultRequestProcessor(ServiceProviderContainer serviceProviderContainer) {
        this.serviceProviderContainer = serviceProviderContainer;
    }

    @Override
    public void addRequestProcessFilter(RequestProcessFilter requestProcessFilter) {
        filters.add(requestProcessFilter);
    }

    @Override
    public void registerGlobalFlowController(FlowController... flowControllers) {
        this.flowControllers = flowControllers;
    }

    @Override
    public RequestCommandProcessor requestCommandProcessor() {
        this.requestCommandProcessor = new RequestCommandProcessor() {

            @Override
            public ResponseCommand process(ChannelHandlerContext context, RequestCommand request, Throwable e) {
                Serializer serializer = SerializerFactory.serializer(SerializerType.parse(request.getSerializerCode()));

                String message = "[OVERLOAD]system busy, start flow control for a while";
                ResponseWrapper responseWrapper = new ResponseWrapper();
                responseWrapper.setResult(message);
                logger.error(message, e);

                if (!request.isOneWay()) {
                    ResponseCommand responseCommand = RemotingCommandFactory.createResponseCommand(
                            request.getSerializerCode(),
                            serializer.serialize(responseWrapper),
                            request.getInvokeId()
                    );
                    responseCommand.setStatus(ResponseStatus.SYSTEM_BUSY.value());
                    return responseCommand;
                }
                return null;
            }

            @Override
            public ResponseCommand process(ChannelHandlerContext context, RequestCommand request) {
                Serializer serializer = SerializerFactory.serializer(SerializerType.parse(request.getSerializerCode()));

                switch (request.getMessageCode()) {
                    case ProtocolHead.RPC_REQUEST: {
                    }
                    case ProtocolHead.ONEWAY_REQUEST: {
                        RequestWrapper requestWrapper = serializer.deserialize(request.getBody(), RequestWrapper.class);

                        ResponseWrapper responseWrapper = new ResponseWrapper();
                        if (rejectRequest()) {
                            String message = "[REJECT_REQUEST] system busy, start flow control for a while";
                            responseWrapper.setResult(message);
                            logger.warn(message);
                            if (request.isOneWay()) {
                                ResponseCommand responseCommand = RemotingCommandFactory.createResponseCommand(
                                        request.getSerializerCode(),
                                        serializer.serialize(responseWrapper),
                                        request.getInvokeId()
                                );
                                responseCommand.setStatus(ResponseStatus.FLOW_CONTROL.value());
                            }
                        }

                        ServiceWrapper serviceWrapper = serviceProviderContainer.lookupService(requestWrapper.getServiceMeta().directory());
                        Object result = null;
                        if (serviceWrapper == null) {
                            String message = String.format(
                                    "%s service: [%s] not found",
                                    context.channel(),
                                    requestWrapper.getServiceMeta()
                            );
                            logger.error(message);
                        } else {
                            if (filters.size() > 0) {
                                for (RequestProcessFilter filter : filters) {
                                    filter.filter(requestWrapper, serviceWrapper);
                                }
                            }
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
                        if (request.isOneWay()) {
                            return null;
                        }

                        if (result instanceof Throwable) {
                            responseWrapper.setCase((Throwable) result);
                        } else {
                            responseWrapper.setResult(result);
                        }

                        if (filters.size() > 0) {
                            for (RequestProcessFilter filter : filters) {
                                filter.filter(responseWrapper);
                            }
                        }

                        ResponseCommand responseCommand = RemotingCommandFactory.createResponseCommand(
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
                        return responseCommand;
                    }
                    default: {
                        String errorMessage = String.format("DefaultProviderProcessor Unsupported MessageCode: %d",
                                request.getMessageCode());
                        throw new UnsupportedOperationException(errorMessage);
                    }
                }

            }

            private boolean rejectRequest() {
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
        };
        return requestCommandProcessor;
    }

    @Override
    public ResponseWrapper process(RequestWrapper request) {

        return null;
    }
}