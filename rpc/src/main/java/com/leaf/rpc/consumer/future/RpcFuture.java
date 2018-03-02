package com.leaf.rpc.consumer.future;

import com.leaf.common.utils.AnyThrow;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RpcFuture<V> implements InvokeFuture<V> {

    private V v;

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private RpcFutureListener<V> listener;

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
    public V get(long timeOut, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public V get() {
        try {
            countDownLatch.await();
        } catch (InterruptedException ignore) {
            AnyThrow.throwUnchecked(ignore);
        }
        return v;
    }

    public void addListener(RpcFutureListener<V> listener) {
        this.listener = listener;
    }

    public RpcFutureListener<V> getListener() {
        return listener;
    }
}
