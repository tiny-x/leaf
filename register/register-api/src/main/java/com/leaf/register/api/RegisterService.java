package com.leaf.register.api;

import com.leaf.common.UnresolvedAddress;
import com.leaf.register.api.model.RegisterMeta;
import com.leaf.register.api.model.SubscribeMeta;

import java.util.List;

public interface RegisterService {

    /**
     * 注册
     *
     * @param registerMeta
     */
    void register(RegisterMeta registerMeta);

    /**
     * 注销
     *
     * @param RegisterMeta
     */
    void unRegister(RegisterMeta RegisterMeta);

    /**
     * 订阅服务（ServiceMeta）
     *
     * @param subscribeMeta
     * @param notifyListener
     */
    void subscribe(SubscribeMeta subscribeMeta, NotifyListener notifyListener);

    /**
     * 订阅所有组信息（group）
     *
     * @param notifyListener
     */
    void subscribeGroup(NotifyListener notifyListener);

    /**
     * 查找
     *
     * @param RegisterMeta
     * @return
     */
    List<RegisterMeta> lookup(RegisterMeta RegisterMeta);

    /**
     * 机器所有服务下线通知
     *
     * @param address
     * @param listener
     */
    void offlineListening(UnresolvedAddress address, OfflineListener listener);

    /**
     * 注册中心（default, zookeeper）
     *
     * @return
     */
    RegisterType registerType();

    /**
     * 连接注册中心
     *
     * @param addresses
     */
    void connectToRegistryServer(String addresses);

}
