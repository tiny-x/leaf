package com.leaf.rpc.consumer.future;

public interface InvokeFutureGroup<V> extends InvokeFuture<V> {

    InvokeFuture<V>[] futures();
}
