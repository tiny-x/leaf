package com.leaf.rpc.consumer.future;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultInvokeFuture<V> implements InvokeFuture<V> {

    private V v;

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private AtomicBoolean notifyOnce = new AtomicBoolean(false);

    private InvokeFutureListener<V> listener;

    private final Class<V> returnType;

    private final long timeoutMillis;

    public DefaultInvokeFuture(Class<V> returnType, long timeoutMillis) {
        this.returnType = returnType;
        this.timeoutMillis = timeoutMillis;
    }

    @Override
    public void complete(V v) {
        this.v = v;
        countDownLatch.countDown();
    }

    @Override
    public boolean isDone() {
        return countDownLatch.getCount() == 0;
    }

    @Override
    public V get(long timeout, TimeUnit timeUnit) throws Throwable {
        countDownLatch.await(timeout, timeUnit);
        if (v instanceof Throwable) {
            throw (Throwable) v;
        }
        return v;
    }

    @Override
    public V get() throws Throwable {
        countDownLatch.await();
        if (v instanceof Throwable) {
            throw (Throwable) v;
        }
        return v;
    }

    @Override
    public void addListener(InvokeFutureListener<V> listener) {
        this.listener = listener;
        if (isDone()) {
            notifyListener(v);
        }
    }

    @Override
    public void notifyListener(Object x) {
        if (listener != null && notifyOnce.compareAndSet(false, true)) {
            if (x instanceof Throwable) {
                listener.failure((Throwable) x);
            } else {
                listener.complete((V) x);
            }
        }
    }

    @Override
    public InvokeFutureListener<V> getListener() {
        return listener;
    }

    @Override
    public Class<V> returnType() {
        return returnType;
    }
}
