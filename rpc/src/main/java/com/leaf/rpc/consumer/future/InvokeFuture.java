package com.leaf.rpc.consumer.future;

import java.util.concurrent.TimeUnit;

public interface InvokeFuture<V> {

    void complete(V v);

    boolean isDone();

    V get();

    V get(long timeOut, TimeUnit timeUnit);
}
