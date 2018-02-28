package com.leaf.rpc.consumer.cluster;

import com.leaf.rpc.Request;
import com.leaf.rpc.consumer.InvokeType;
import com.leaf.rpc.consumer.dispatcher.Dispatcher;
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
    public Object invoke(Request request, InvokeType invokeType) throws Exception {
        Object result = invoke0(request, 0, invokeType);
        return result;
    }

    private Object invoke0(Request request, int tryCount, InvokeType invokeType) throws Exception {
        try {
            tryCount ++;
            return dispatcher.dispatch(request, invokeType);
        } catch (Exception e) {
            if (tryCount <= retries) {
                logger.warn("[FAILOVER] tryCount: {} directory: {}, method: {}",
                        tryCount,
                        request.getRequestWrapper().getServiceMeta().directory(),
                        request.getRequestWrapper().getMethodName(), e);
                return invoke0(request, tryCount, invokeType);
            } else {
                throw e;
            }
        }
    }
}
