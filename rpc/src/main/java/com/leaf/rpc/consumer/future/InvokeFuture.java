package com.leaf.rpc.consumer.future;

import java.util.concurrent.TimeUnit;

public interface InvokeFuture<V> {

    void complete(V v);

    boolean isDone();

    V get() throws Throwable;

    V get(long timeout, TimeUnit timeUnit) throws Throwable;

    void addListener(InvokeFutureListener<V> listener);

    InvokeFutureListener<V> getListener();

    void notifyListener(Object x);

    Class<V> returnType();
}
