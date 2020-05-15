package com.leaf.rpc.exector;

import java.util.concurrent.ExecutorService;

public interface ExecutorFactory {

    /**
     *
     * @param
     * @return
     */
    ExecutorService createExecutorService(ProcessThreadFactory threadFactory);
}
