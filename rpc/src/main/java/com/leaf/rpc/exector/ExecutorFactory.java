package com.leaf.rpc.exector;

import java.util.concurrent.ExecutorService;

public interface ExecutorFactory {

    ExecutorService createExecutorService();
}
