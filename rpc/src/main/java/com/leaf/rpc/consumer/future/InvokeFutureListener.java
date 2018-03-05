package com.leaf.rpc.consumer.future;

public interface InvokeFutureListener<T> {

    void complete(T result);

    void failure(Throwable cause);
}
