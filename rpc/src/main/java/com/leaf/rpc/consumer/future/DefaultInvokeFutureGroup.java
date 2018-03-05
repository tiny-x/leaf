package com.leaf.rpc.consumer.future;

import java.util.concurrent.TimeUnit;

public class DefaultInvokeFutureGroup<V>  implements InvokeFutureGroup<V>{

    private InvokeFuture<V>[] futures;

    public DefaultInvokeFutureGroup(InvokeFuture<V>[] futures) {
        this.futures = futures;
    }

    @Override
    public InvokeFuture<V>[] futures() {
        return futures;
    }

    @Override
    public void addListener(InvokeFutureListener<V> listener) {
        if (futures != null && futures.length > 0) {
            for (InvokeFuture<V> future : futures) {
                future.addListener(listener);
            }
        }
    }

    @Override
    public void complete(V v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDone() {
        for (InvokeFuture<V> future : futures) {
            if (!future.isDone()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public V get() throws Throwable {
        throw new UnsupportedOperationException();
    }

    @Override
    public V get(long timeout, TimeUnit timeUnit) throws Throwable {
        throw new UnsupportedOperationException();
    }

    @Override
    public void notifyListener(Object x) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<V> returnType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public InvokeFutureListener<V> getListener() {
        throw new UnsupportedOperationException();
    }
}
