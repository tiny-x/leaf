package com.leaf.remoting.api;

import com.leaf.remoting.api.future.ResponseFuture;

public interface InvokeCallback<T> {

    void operationComplete(final ResponseFuture<T> responseFuture) ;
}
