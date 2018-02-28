package com.leaf.rpc;

import com.leaf.common.model.RequestWrapper;
import com.leaf.remoting.api.payload.RequestCommand;

public class Request {

    private RequestCommand requestCommand;

    private RequestWrapper requestWrapper;

    public Request() {
    }

    public Request(RequestCommand requestCommand, RequestWrapper requestWrapper) {
        this.requestCommand = requestCommand;
        this.requestWrapper = requestWrapper;
    }

    public RequestCommand getRequestCommand() {
        return requestCommand;
    }

    public void setRequestCommand(RequestCommand requestCommand) {
        this.requestCommand = requestCommand;
    }

    public RequestWrapper getRequestWrapper() {
        return requestWrapper;
    }

    public void setRequestWrapper(RequestWrapper requestWrapper) {
        this.requestWrapper = requestWrapper;
    }
}
