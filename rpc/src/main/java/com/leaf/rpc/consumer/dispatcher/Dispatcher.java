package com.leaf.rpc.consumer.dispatcher;

import com.leaf.remoting.exception.RemotingException;
import com.leaf.rpc.Request;
import com.leaf.rpc.consumer.InvokeType;

/**
 *
 */
public interface Dispatcher {

    <T> T dispatch(Request request, InvokeType invokeType) throws RemotingException, InterruptedException;

    Dispatcher timeoutMillis(long timeoutMillis);

}
