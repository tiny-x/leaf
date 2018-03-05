package com.leaf.rpc.consumer.future;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class InvokeFutureContext {

    private static final ThreadLocal<InvokeFuture<?>> INVOKE_FUTURE = new ThreadLocal<>();

    public static InvokeFuture getInvokeFuture() {
        InvokeFuture<?> defaultInvokeFuture = checkNotNull(INVOKE_FUTURE.get(), "future is null");
        INVOKE_FUTURE.remove();
        return defaultInvokeFuture;
    }

    public static InvokeFutureGroup getInvokeFutureGroup() {
        InvokeFuture<?> defaultInvokeFuture = checkNotNull(INVOKE_FUTURE.get(), "future is null");
        checkState(defaultInvokeFuture instanceof InvokeFutureGroup);
        INVOKE_FUTURE.remove();
        return (InvokeFutureGroup) defaultInvokeFuture;
    }

    public static void setInvokeFuture(InvokeFuture future) {
        INVOKE_FUTURE.set(future);
    }

}
