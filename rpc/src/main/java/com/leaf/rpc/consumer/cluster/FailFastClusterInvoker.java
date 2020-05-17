package com.leaf.rpc.consumer.cluster;

import com.leaf.rpc.provider.process.RequestWrapper;
import com.leaf.rpc.consumer.InvokeType;
import com.leaf.rpc.consumer.dispatcher.Dispatcher;
import com.leaf.rpc.consumer.future.InvokeFuture;

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
    public <T> InvokeFuture<T> invoke(RequestWrapper request, Class<T> returnType, InvokeType invokeType) throws Throwable {
        return dispatcher.dispatch(request, returnType, invokeType);
    }
}
