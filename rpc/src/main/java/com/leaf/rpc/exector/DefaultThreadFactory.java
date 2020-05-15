package com.leaf.rpc.exector;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yefei
 */
public class DefaultThreadFactory implements ProcessThreadFactory {

    private final AtomicInteger atomicInteger = new AtomicInteger(0);

    @Override
    public ProcessThread newThread(Runnable r) {
        ProcessThread thread = new ProcessThread(r);
        thread.setName("REQUEST_PROCESS#PROVIDER-" + atomicInteger.getAndIncrement());
        return thread;
    }

}
