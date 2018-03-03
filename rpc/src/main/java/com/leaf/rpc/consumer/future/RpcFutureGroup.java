package com.leaf.rpc.consumer.future;

public class RpcFutureGroup<V> {

    private InvokeFuture<V>[] futures;

    public RpcFutureGroup(InvokeFuture<V>[] futures) {
        this.futures = futures;
    }

    public InvokeFuture<V>[] futures() {
        return futures;
    }

    public void addListener(RpcFutureListener<V> listener) {
        if (futures != null && futures.length > 0) {
            for (InvokeFuture<V> future : futures) {
                future.addListener(listener);
            }
        }
    }

}
