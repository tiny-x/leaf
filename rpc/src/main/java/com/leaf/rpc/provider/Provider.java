package com.leaf.rpc.provider;

import com.leaf.common.model.Directory;
import com.leaf.common.model.ServiceWrapper;
import com.leaf.rpc.controller.FlowController;
import com.leaf.rpc.local.ServiceRegistry;

public interface Provider {

    /**
     * 启动
     */
    void start();

    /**
     * 本地容器查找
     *
     * @param directory
     * @return
     */
    ServiceWrapper lookupService(Directory directory);

    /**
     * 本地注册（本地内存缓存服务实现类）
     *
     * @return
     */
    ServiceRegistry serviceRegistry();

    /**
     * 连接注册中心
     *
     * @param addressess
     */
    void connectToRegistryServer(String addressess);

    /**
     * 发布服务到注册中心
     *
     * @param serviceWrapper
     */
    void publishService(ServiceWrapper serviceWrapper);

    /**
     * 注册全局流量控制器
     *
     */
    void registerGlobalFlowController(FlowController... flowControllers);

    /**
     *
     * @return
     */
    FlowController[] globalFlowController();
}
