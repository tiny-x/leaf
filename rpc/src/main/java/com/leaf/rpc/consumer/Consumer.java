package com.leaf.rpc.consumer;

import com.leaf.common.UnresolvedAddress;
import com.leaf.common.model.Directory;
import com.leaf.common.model.RegisterMeta;
import com.leaf.register.api.NotifyListener;
import com.leaf.register.api.OfflineListener;
import com.leaf.register.api.RegisterService;
import com.leaf.remoting.api.RpcClient;

import java.util.List;

public interface Consumer {

    RpcClient client() ;

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
     * @param addressess
     */
    void connectToRegistryServer(String addressess);

    /**
     * 从注册中心订阅一个服务.
     */
    void subscribe(Directory directory, NotifyListener listener);

    /**
     * 服务下线通知.
     */
    void offlineListening(UnresolvedAddress address, OfflineListener listener);

    /**
     * 查找服务
     *
     * @param registerMeta
     * @return
     */
    List<RegisterMeta> lookup(RegisterMeta registerMeta);

}
