package com.leaf.remoting.api.future;

import com.leaf.common.concurrent.SemaphoreReleaseOnce;
import com.leaf.remoting.api.InvokeCallback;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ResponseFuture<T> {

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private final long beginTimestamp = System.currentTimeMillis();

    private AtomicBoolean atomicBoolean = new AtomicBoolean(false);

    private SemaphoreReleaseOnce semaphoreReleaseOnce;

    private InvokeCallback<T> invokeCallback;

    private Throwable cause;

    private T result;

    private long timeoutMillis;

    public void executeInvokeCallback() {
        if (invokeCallback != null) {
            // 异常 超时可能导致回调函数 多次执行
            if (atomicBoolean.compareAndSet(false, true)) {
                invokeCallback.operationComplete(this);
            }
        }
    }

    public ResponseFuture(long timeoutMillis) {
        this(timeoutMillis, null, null);
    }

    public ResponseFuture(long timeoutMillis, InvokeCallback invokeCallback, Semaphore semaphore) {
        this.timeoutMillis = timeoutMillis;
        this.invokeCallback = invokeCallback;
        if (semaphore != null) {
            this.semaphoreReleaseOnce = new SemaphoreReleaseOnce(semaphore);
        }
    }

    public T get() throws InterruptedException {
        countDownLatch.await();
        return result;
    }

    public T get(long timeout, TimeUnit timeUnit) throws InterruptedException {
        countDownLatch.await(timeout, timeUnit);
        return result;
    }

    public void complete(T t) {
        this.result = t;
        countDownLatch.countDown();
    }

    public T result() {
        return result;
    }

    public InvokeCallback<T> getInvokeCallback() {
        return invokeCallback;
    }

    public void failure(Throwable cause) {
        this.cause = cause;
    }

    public Throwable cause() {
        return cause;
    }

    public void release() {
        if (semaphoreReleaseOnce != null) {
            semaphoreReleaseOnce.release();
        }
    }

    public boolean isTimeout() {
        return (System.currentTimeMillis() - beginTimestamp) > timeoutMillis;
    }

    public long getBeginTimestamp() {
        return beginTimestamp;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }
}
