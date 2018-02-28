package com.leaf.rpc.consumer.future;

public interface RpcFutureListener<T> {

    void complete(T result);

    void failure(Throwable cause);
}
