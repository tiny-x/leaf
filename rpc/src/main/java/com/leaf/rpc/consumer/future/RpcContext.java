package com.leaf.rpc.consumer.future;

import static com.google.common.base.Preconditions.checkNotNull;

public class RpcContext {

    private static final ThreadLocal<RpcFuture<?>> threadLocal = new ThreadLocal<>();

    public static RpcFuture getFuture() {
        RpcFuture<?> rpcFuture = checkNotNull(threadLocal.get(), "future is null");
        threadLocal.remove();
        return rpcFuture;
    }

    public static void setFuture(RpcFuture future) {
        threadLocal.set(future);
    }
}
