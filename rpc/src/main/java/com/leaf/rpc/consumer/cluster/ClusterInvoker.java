package com.leaf.rpc.consumer.cluster;

import com.leaf.rpc.provider.process.RequestWrapper;
import com.leaf.rpc.consumer.InvokeType;
import com.leaf.rpc.consumer.future.InvokeFuture;

public interface ClusterInvoker {

    /**
     * 集群容错策略
     */
    enum Strategy {
        FAIL_FAST,  // 快速失败
        FAIL_OVER,  // 失败重试
        FAIL_SAFE,  // 失败安全
        ;
        public static Strategy parse(String name) {
            for (Strategy s : values()) {
                if (s.name().equalsIgnoreCase(name)) {
                    return s;
                }
            }
            return null;
        }

        public static Strategy getDefault() {
            return FAIL_FAST;
        }
    }

    Strategy strategy();

    <T> InvokeFuture<T> invoke(RequestWrapper request, Class<T> returnType, InvokeType invokeType) throws Throwable;
}
