package com.leaf.rpc.provider.process;

import com.leaf.remoting.api.RequestCommandProcessor;
import com.leaf.rpc.controller.FlowController;

/**
 * rpc层请求处理器
 * @author yefei
 */
public interface RequestProcessor {

    /**
     * remoting 层处理
     *
     * @return
     */
    RequestCommandProcessor requestCommandProcessor();

    /**
     * 注册全局流量控制器
     */
    void registerGlobalFlowController(FlowController... flowControllers);

    /**
     *
     * @param requestProcessFilter
     */
    void addRequestProcessFilter(RequestProcessFilter requestProcessFilter);

}
