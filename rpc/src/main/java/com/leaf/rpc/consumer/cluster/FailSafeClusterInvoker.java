package com.leaf.rpc.consumer.cluster;

import com.leaf.remoting.api.RequestWrapper;
import com.leaf.rpc.consumer.InvokeType;
import com.leaf.rpc.consumer.dispatcher.Dispatcher;
import com.leaf.rpc.consumer.future.FailSafeInvokeFuture;
import com.leaf.rpc.consumer.future.InvokeFuture;

/**
 * 同步调用，异常不影响消费者中断
 */
public class FailSafeClusterInvoker implements ClusterInvoker {

    private final Dispatcher dispatcher;

    public FailSafeClusterInvoker(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public Strategy strategy() {
        return Strategy.FAIL_SAFE;
    }

    @Override
    public <T> InvokeFuture<T> invoke(RequestWrapper request, Class<T> returnType, InvokeType invokeType) throws Throwable {
        InvokeFuture<T> invokeFuture = dispatcher.dispatch(request, returnType, invokeType);
        return FailSafeInvokeFuture.with(invokeFuture);
    }
}
