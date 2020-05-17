package com.leaf.rpc.consumer.cluster;

import com.leaf.rpc.provider.process.RequestWrapper;
import com.leaf.rpc.consumer.InvokeType;
import com.leaf.rpc.consumer.dispatcher.Dispatcher;
import com.leaf.rpc.consumer.future.InvokeFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FailOverClusterInvoker implements ClusterInvoker {

    private static final Logger logger = LoggerFactory.getLogger(FailOverClusterInvoker.class);

    private final Dispatcher dispatcher;

    /**
     * 重试次数（不包含第一次）
     */
    private final int retries;

    public FailOverClusterInvoker(Dispatcher dispatcher, int retries) {
        this.dispatcher = dispatcher;
        this.retries = (retries < 0) ? 0 : retries;
    }

    @Override
    public Strategy strategy() {
        return Strategy.FAIL_OVER;
    }

    @Override
    public <T> InvokeFuture<T> invoke(RequestWrapper request, Class<T> returnType, InvokeType invokeType) throws Throwable {
       InvokeFuture<T> result = invoke0(request, returnType, invokeType, 0);
        return result;
    }

    private <T> InvokeFuture<T> invoke0(RequestWrapper request, Class<T> returnType, InvokeType invokeType, int tryCount) throws Throwable {
        try {
            tryCount ++;
            return dispatcher.dispatch(request, returnType, invokeType);
        } catch (Throwable e) {
            if (tryCount <= retries) {
                logger.warn("[FAILOVER] tryCount: {} directory: {}, method: {}",
                        tryCount,
                        request.getServiceMeta().directory(),
                        request.getMethodName(), e);
                return invoke0(request, returnType, invokeType, tryCount);
            } else {
                throw e;
            }
        }
    }
}
