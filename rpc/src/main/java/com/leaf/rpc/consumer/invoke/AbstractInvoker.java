package com.leaf.rpc.consumer.invoke;

import com.leaf.common.model.RequestWrapper;
import com.leaf.common.model.ServiceMeta;
import com.leaf.common.utils.Reflects;
import com.leaf.rpc.Request;
import com.leaf.rpc.consumer.InvokeType;
import com.leaf.rpc.consumer.StrategyConfig;
import com.leaf.rpc.consumer.cluster.ClusterInvoker;
import com.leaf.rpc.consumer.cluster.FailFastClusterInvoker;
import com.leaf.rpc.consumer.cluster.FailOverClusterInvoker;
import com.leaf.rpc.consumer.cluster.FailSafeClusterInvoker;
import com.leaf.rpc.consumer.dispatcher.Dispatcher;
import com.leaf.rpc.consumer.future.InvokeFuture;
import com.leaf.rpc.consumer.future.InvokeFutureContext;

public abstract class AbstractInvoker {

    protected String application;

    protected Dispatcher dispatcher;

    protected ServiceMeta serviceMeta;

    protected StrategyConfig strategyConfig;

    protected InvokeType invokeType;

    public AbstractInvoker(
            String application,
            Dispatcher dispatcher,
            ServiceMeta serviceMeta,
            StrategyConfig strategyConfig,
            InvokeType invokeType) {
        this.application = application;
        this.dispatcher = dispatcher;
        this.serviceMeta = serviceMeta;
        this.strategyConfig = strategyConfig;
        this.invokeType = invokeType;
    }

    public <T> T doInvoke(String methodName, Class<T> returnType, Object... args) throws Throwable {
        RequestWrapper requestWrapper = new RequestWrapper();
        requestWrapper.setApplication(application);
        requestWrapper.setMethodName(methodName);
        requestWrapper.setArgs(args);
        requestWrapper.setServiceMeta(serviceMeta);

        Request request = new Request();
        request.setRequestWrapper(requestWrapper);

        ClusterInvoker clusterInvoker = createClusterInvoker(dispatcher, strategyConfig);
        InvokeFuture<T> invokeFuture = clusterInvoker.invoke(request, returnType, invokeType);
        if (invokeType == InvokeType.SYNC) {
            return invokeFuture.get();
        } else {
            InvokeFutureContext.setInvokeFuture(invokeFuture);
            return (T) Reflects.getTypeDefaultValue(returnType);
        }
    }

    private ClusterInvoker createClusterInvoker(Dispatcher dispatcher, StrategyConfig strategy) {
        ClusterInvoker.Strategy s = strategy.getStrategy();
        switch (s) {
            case FAIL_FAST:
                return new FailFastClusterInvoker(dispatcher);
            case FAIL_OVER:
                return new FailOverClusterInvoker(dispatcher, strategy.getRetries());
            case FAIL_SAFE:
                return new FailSafeClusterInvoker(dispatcher);
            default:
                throw new UnsupportedOperationException("unsupported strategy: " + strategy);
        }
    }
}
