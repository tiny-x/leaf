package com.leaf.rpc.consumer.future;

import com.leaf.common.utils.Reflects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class FailSafeInvokeFuture<V> implements InvokeFuture<V> {

    private final static Logger logger = LoggerFactory.getLogger(FailSafeInvokeFuture.class);

    private InvokeFuture<V> invokeFuture;

    private FailSafeInvokeFuture(InvokeFuture<V> invokeFuture) {
        this.invokeFuture = invokeFuture;
    }

    public static <V> FailSafeInvokeFuture<V> with(InvokeFuture<V> invokeFuture) {
        return new FailSafeInvokeFuture<V>(invokeFuture);
    }

    @Override
    public void complete(V v) {
        invokeFuture.complete(v);
    }

    @Override
    public boolean isDone() {
        return invokeFuture.isDone();
    }

    @Override
    public V get(long timeout, TimeUnit timeUnit) throws Throwable {
        try {
            V v = invokeFuture.get(timeout, timeUnit);
            return v;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            return (V) Reflects.getTypeDefaultValue(returnType());
        }
    }

    @Override
    public V get() throws Throwable {
        try {
            V v = invokeFuture.get();
            return v;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            return (V) Reflects.getTypeDefaultValue(returnType());
        }
    }

    @Override
    public void addListener(InvokeFutureListener<V> listener) {
        invokeFuture.addListener(listener);
    }

    @Override
    public void notifyListener(Object x) {
        invokeFuture.notifyListener(x);
    }

    @Override
    public InvokeFutureListener<V> getListener() {
        return invokeFuture.getListener();
    }

    @Override
    public Class<V> returnType() {
        return invokeFuture.returnType();
    }
}
