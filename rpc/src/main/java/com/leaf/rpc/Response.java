package com.leaf.rpc;

import com.leaf.common.model.ResponseWrapper;
import com.leaf.remoting.api.payload.ResponseCommand;

public class Response {

    private ResponseCommand responseCommand;

    private ResponseWrapper responseWrapper;

    public Response() {
    }

    public Response(ResponseCommand responseCommand, ResponseWrapper responseWrapper) {
        this.responseCommand = responseCommand;
        this.responseWrapper = responseWrapper;
    }

    public ResponseCommand getResponseCommand() {
        return responseCommand;
    }

    public void setResponseCommand(ResponseCommand responseCommand) {
        this.responseCommand = responseCommand;
    }

    public ResponseWrapper getResponseWrapper() {
        return responseWrapper;
    }

    public void setResponseWrapper(ResponseWrapper responseWrapper) {
        this.responseWrapper = responseWrapper;
    }
}
