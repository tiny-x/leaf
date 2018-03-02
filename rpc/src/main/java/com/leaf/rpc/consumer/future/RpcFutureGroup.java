package com.leaf.rpc.consumer.future;

public class RpcFutureGroup<V> {

    private InvokeFuture<V>[] futures;

    public RpcFutureGroup(InvokeFuture<V>[] futures) {
        this.futures = futures;
    }

    public InvokeFuture<V>[] Futures() {
        return futures;
    }
}
