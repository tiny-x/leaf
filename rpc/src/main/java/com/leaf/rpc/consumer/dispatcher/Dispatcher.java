package com.leaf.rpc.consumer.dispatcher;

import com.leaf.remoting.exception.RemotingException;
import com.leaf.rpc.Request;
import com.leaf.rpc.consumer.InvokeType;

/**
 *
 */
public interface Dispatcher {

    <T> T dispatch(Request request, Class<?> returnType, InvokeType invokeType) throws Throwable;

    Dispatcher timeoutMillis(long timeoutMillis);

}
