package com.leaf.rpc.exector;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author yefei
 */
public class ThreadPoolExecutorFactory implements ExecutorFactory {

    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    @Override
    public ExecutorService createExecutorService(ProcessThreadFactory threadFactory) {
        return new ThreadPoolExecutor(
                AVAILABLE_PROCESSORS << 1,
                512,
                120L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue(32768),
                threadFactory
        );
    }
}
