package com.leaf.register.api;

import com.leaf.common.UnresolvedAddress;
import com.leaf.common.model.ServiceMeta;
import com.leaf.register.api.model.RegisterMeta;
import com.leaf.register.api.model.SubscribeMeta;

import java.util.List;

public interface RegisterService {

    /**
     *
     * @return
     */
    void setNamespace(String namespace);

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
     * 订阅服务注册者信息
     *
     * @param subscribeMeta
     * @param notifyListener
     */
    void subscribeRegisterMeta(SubscribeMeta subscribeMeta, NotifyListener<RegisterMeta> notifyListener);

    /**
     * 订阅服务订阅者
     *
     * @param serviceMeta
     * @param notifyListener
     */
    void subscribeSubscribeMeta(ServiceMeta serviceMeta, NotifyListener<SubscribeMeta> notifyListener);

    /**
     * 查找所有的服务
     *
     * @return
     */
    List<ServiceMeta> lookup();

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

    /**
     * close
     */
    void shutdown();

}
