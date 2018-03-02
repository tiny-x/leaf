package com.leaf.rpc.consumer.future;

import static com.google.common.base.Preconditions.checkNotNull;

public class RpcContext {

    private static final ThreadLocal<RpcFuture<?>> RPC_FUTURE = new ThreadLocal<>();

    private static final ThreadLocal<RpcFutureGroup<?>> RPC_FUTURE_GROUP = new ThreadLocal<>();

    public static RpcFuture getFuture() {
        RpcFuture<?> rpcFuture = checkNotNull(RPC_FUTURE.get(), "future is null");
        RPC_FUTURE.remove();
        return rpcFuture;
    }

    public static void setFuture(RpcFuture future) {
        RPC_FUTURE.set(future);
    }

    public static RpcFutureGroup getRpcFutureGroup() {
        RpcFutureGroup<?> rpcFutureGroup = checkNotNull(RPC_FUTURE_GROUP.get(), "FutureGroup is null");
        RPC_FUTURE_GROUP.remove();
        return rpcFutureGroup;
    }

    public static void setRpcFutureGroup(RpcFutureGroup rpcFutureGroup) {
        RPC_FUTURE_GROUP.set(rpcFutureGroup);
    }
}
