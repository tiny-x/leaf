package com.leaf.rpc.provider.process;

import com.leaf.remoting.api.RequestCommandProcessor;
import com.leaf.rpc.controller.FlowController;

public interface RequestProcessor {

    /**
     * rpc层处理
     *
     * @param request
     * @return
     */
    ResponseWrapper process(RequestWrapper request);

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
