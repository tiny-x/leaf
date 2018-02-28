package com.leaf.rpc.exector;

import java.util.concurrent.*;

/**
 * @author yefei
 * @date 2018-02-26 9:55
 */
public class ThreadPoolExectorFactory implements ExectorFactory {

    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    @Override
    public ExecutorService createExecutorService() {
        return new ThreadPoolExecutor(
                AVAILABLE_PROCESSORS << 1,
                512,
                120L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue(32768),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setName("REQUEST_PROCESS#PROVIDER");
                        return thread;
                    }
                }
        );
    }
}
