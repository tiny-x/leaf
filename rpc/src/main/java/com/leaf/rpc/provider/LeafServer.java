package com.leaf.rpc.provider;

import com.leaf.common.model.Directory;
import com.leaf.rpc.controller.FlowController;
import com.leaf.rpc.local.ServiceRegistry;
import com.leaf.rpc.local.ServiceWrapper;
import com.leaf.rpc.provider.process.RequestProcessFilter;

/**
 * rpc 服务端
 * @author yefei
 */
public interface LeafServer {

    /**
     * 启动
     */
    void start();

    /**
     *
     */
    void shutdown();

    /**
     * 本地容器查找
     *
     * @param directory
     * @return
     */
    ServiceWrapper lookupService(Directory directory);

    /**
     *
     * @return
     */
    String application();

    /**
     * 本地注册（本地内存缓存服务实现类）
     *
     * @return
     */
    ServiceRegistry serviceRegistry();

    /**
     * 连接注册中心
     *
     * @param addresses
     */
    void connectToRegistryServer(String addresses);

    /**
     * 发布服务到注册中心
     *
     * @param serviceWrapper
     */
    void publishService(ServiceWrapper serviceWrapper);

    /**
     *
     * @param requestProcessFilter
     */
    void addRequestProcessFilter(RequestProcessFilter requestProcessFilter);

    /**
     * 注册全局流量控制器
     */
    void registerGlobalFlowController(FlowController... flowControllers);

    /**
     * @return
     */
    FlowController[] globalFlowController();
}
