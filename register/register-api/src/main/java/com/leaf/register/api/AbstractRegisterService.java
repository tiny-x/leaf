package com.leaf.register.api;


import com.leaf.common.UnresolvedAddress;
import com.leaf.common.concurrent.ConcurrentSet;
import com.leaf.common.model.RegisterMeta;
import com.leaf.common.model.ServiceMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractRegisterService implements RegisterService {

    /**
     * logger
     */
    private final static Logger logger = LoggerFactory.getLogger(AbstractRegisterService.class);

    /**
     * 订阅者监听器
     */
    private final ConcurrentMap<ServiceMeta, NotifyListener> subscribeListeners = new ConcurrentHashMap<>();


    private final ConcurrentMap<UnresolvedAddress, CopyOnWriteArrayList<OfflineListener>> offlineListeners =
            new ConcurrentHashMap<>();

    /**
     * 服务提供者(断线重连是重现注册服务)
     */
    protected final ConcurrentSet<RegisterMeta> providers = new ConcurrentSet<>();

    /**
     * 已经订阅的服务
     */
    protected final ConcurrentSet<ServiceMeta> consumers = new ConcurrentSet<>();


    @Override
    public void register(RegisterMeta registerMeta) {
        logger.info("[REGISTER] register service: {}", registerMeta);
        providers.add(registerMeta);
        doRegister(registerMeta);
    }

    @Override
    public void unRegister(RegisterMeta registerMeta) {
        logger.info("[UN_REGISTER] unRegister service: {}", registerMeta);
        consumers.remove(registerMeta);
        doUnRegister(registerMeta);
    }

    @Override
    public void subscribe(ServiceMeta serviceMeta, NotifyListener notifyListener) {
        logger.info("[SUBSCRIBE] subscribe service: {}", serviceMeta);
        subscribeListeners.put(serviceMeta, notifyListener);
        consumers.add(serviceMeta);
        doSubscribe(serviceMeta);
    }

    @Override
    public void offlineListening(UnresolvedAddress address, OfflineListener listener) {
        CopyOnWriteArrayList<OfflineListener> offlineListenerList = offlineListeners.get(address);
        if (offlineListenerList == null) {
            CopyOnWriteArrayList<OfflineListener> newOfflineListenerList = new CopyOnWriteArrayList<>();
            offlineListenerList = offlineListeners.putIfAbsent(address, newOfflineListenerList);
            if (offlineListenerList == null) {
                offlineListenerList = newOfflineListenerList;
            }
        }
        offlineListenerList.add(listener);
    }

    public void offline(UnresolvedAddress address) {
        logger.info("[OFFLINE] provider offline: {}", address);
        // remove & notify
        CopyOnWriteArrayList<OfflineListener> offlineListenerList = offlineListeners.remove(address);
        for (OfflineListener offlineListener : offlineListenerList) {
            offlineListener.offline();
        }
    }

    public void notify(ServiceMeta serviceMeta, NotifyEvent event, List<RegisterMeta> registerMetas) {
        logger.info("[NOTIFY] consumer service: {} notifyEvent：{}, registerMetas: {}",
                serviceMeta,
                event.name(),
                registerMetas);

        if (registerMetas != null && registerMetas.size() > 0) {
            NotifyListener notifyListener = subscribeListeners.get(serviceMeta);
            for (RegisterMeta registerMeta : registerMetas) {
                notifyListener.notify(registerMeta, event);
            }
        }
    }

    @Override
    public List<RegisterMeta> lookup(RegisterMeta RegisterMeta) {
        return null;
    }

    public ConcurrentSet<RegisterMeta> getProviders() {
        return providers;
    }

    public ConcurrentSet<ServiceMeta> getConsumers() {
        return consumers;
    }

    protected abstract void doRegister(RegisterMeta registerMeta);

    protected abstract void doUnRegister(RegisterMeta registerMeta);

    protected abstract void doSubscribe(ServiceMeta serviceMeta);

}
