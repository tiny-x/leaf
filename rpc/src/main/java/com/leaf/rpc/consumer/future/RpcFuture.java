package com.leaf.rpc.consumer.future;

import java.util.concurrent.CountDownLatch;

public class RpcFuture<V> {

    private V v;

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private RpcFutureListener<V> listener;

    public void set(V v) {
        this.v = v;
        countDownLatch.countDown();
    }

    public V get() {
        try {
            countDownLatch.await();
        } catch (InterruptedException ignore) {

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
