package com.leaf.rpc.consumer;

import com.leaf.common.UnresolvedAddress;
import com.leaf.common.model.Directory;
import com.leaf.register.api.NotifyListener;
import com.leaf.register.api.OfflineListener;
import com.leaf.register.api.RegisterService;
import com.leaf.remoting.api.RemotingClient;

/**
 * 服务消费者
 */
public interface LeafClient {

    /**
     * 通信客户端
     * @return
     */
    RemotingClient remotingClient() ;

    /**
     *
     * @return
     */
    String application();

    /**
     * 注册中心
     * @return
     */
    RegisterService registerService();

    /**
     * 直连调用
     * @param address
     */
    void connect(UnresolvedAddress address);

    /**
     * 连接到注册中心
     * @param addresses
     */
    void connectToRegistryServer(String addresses);

    /**
     * 从注册中心订阅一个服务.
     */
    void subscribe(Directory directory, NotifyListener listener);

    /**
     * 服务下线通知.
     */
    void offlineListening(UnresolvedAddress address, OfflineListener listener);

    /**
     *
     */
    void shutdown();
}
