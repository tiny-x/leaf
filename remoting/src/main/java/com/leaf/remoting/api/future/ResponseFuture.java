package com.leaf.remoting.api.future;


import com.leaf.remoting.api.InvokeCallback;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ResponseFuture<T> {

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private InvokeCallback<T> invokeCallback;

    private Throwable cause;

    private T result;

    private volatile boolean success;

    public void executeInvokeCallback() {
        if (invokeCallback != null) {
            invokeCallback.operationComplete(this);
        }
    }

    public ResponseFuture() {
    }

    public ResponseFuture(InvokeCallback invokeCallback) {
        this.invokeCallback = invokeCallback;
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

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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
}
