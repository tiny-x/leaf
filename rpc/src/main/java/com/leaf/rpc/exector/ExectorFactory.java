package com.leaf.rpc.exector;

import java.util.concurrent.ExecutorService;

/**
 * @author yefei
 * @date 2018-02-26 9:48
 */
public interface ExectorFactory {

    ExecutorService createExecutorService();
}
