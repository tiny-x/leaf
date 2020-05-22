package com.leaf.remoting.api;

import com.leaf.common.UnresolvedAddress;
import com.leaf.remoting.api.exception.RemotingException;
import com.leaf.remoting.api.payload.RequestCommand;
import com.leaf.remoting.api.payload.ResponseCommand;

import java.util.concurrent.ExecutorService;

/**
 * @author yefei
 */
public interface RemotingService {

    /**
     * 注册参数处理器
     *
     * @param requestCommandProcessor
     * @param executor
     */
    void registerRequestProcess(RequestCommandProcessor requestCommandProcessor, ExecutorService executor);

    /**
     * start
     */
    void start();

    /**
     * shutdown
     */
    void shutdownGracefully();
}
