package com.leaf.rpc.consumer.cluster;


import com.leaf.rpc.Request;
import com.leaf.rpc.consumer.InvokeType;
import com.leaf.rpc.consumer.dispatcher.Dispatcher;

public class FailFastClusterInvoker implements ClusterInvoker {

    private final Dispatcher dispatcher;

    public FailFastClusterInvoker(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public Strategy strategy() {
        return Strategy.FAIL_FAST;
    }

    @Override
    public Object invoke(Request request, InvokeType invokeType) throws Exception {
        return dispatcher.dispatch(request, invokeType);
    }
}
