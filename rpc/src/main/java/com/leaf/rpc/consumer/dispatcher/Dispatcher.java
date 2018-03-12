package com.leaf.rpc.consumer.dispatcher;

import com.leaf.remoting.api.RequestWrapper;
import com.leaf.rpc.consumer.InvokeType;
import com.leaf.rpc.consumer.future.InvokeFuture;

/**
 *
 */
public interface Dispatcher {

    <T> InvokeFuture<T> dispatch(RequestWrapper request, Class<T> returnType, InvokeType invokeType) throws Throwable;

    Dispatcher timeoutMillis(long timeoutMillis);

}
