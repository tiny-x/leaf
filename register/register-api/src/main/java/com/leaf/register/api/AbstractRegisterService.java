package com.leaf.register.api;

import com.leaf.common.UnresolvedAddress;
import com.leaf.common.concurrent.ConcurrentSet;
import com.leaf.common.model.RegisterMeta;
import com.leaf.common.model.ServiceMeta;
import com.leaf.common.utils.Collections;
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
     * 已经注册的服务(断线重连是重连注册服务)
     */
    protected final ConcurrentSet<RegisterMeta> providerRegisterMetas = new ConcurrentSet<>();

    /**
     * 已经订阅的服务
     */
    protected final ConcurrentSet<ServiceMeta> consumersServiceMeta = new ConcurrentSet<>();


    @Override
    public void register(RegisterMeta registerMeta) {
        logger.info("[REGISTER] register service: {}", registerMeta);
        providerRegisterMetas.add(registerMeta);
        doRegister(registerMeta);
    }

    @Override
    public void unRegister(RegisterMeta registerMeta) {
        logger.info("[UN_REGISTER] unRegister service: {}", registerMeta);
        consumersServiceMeta.remove(registerMeta);
        doUnRegister(registerMeta);
    }

    @Override
    public void subscribe(ServiceMeta serviceMeta, NotifyListener notifyListener) {
        logger.info("[SUBSCRIBE] subscribe service: {}", serviceMeta);
        subscribeListeners.put(serviceMeta, notifyListener);
        consumersServiceMeta.add(serviceMeta);
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

        if (Collections.isNotEmpty(registerMetas)) {
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

    public ConcurrentSet<RegisterMeta> getProviderRegisterMetas() {
        return providerRegisterMetas;
    }

    public ConcurrentSet<ServiceMeta> getConsumersServiceMeta() {
        return consumersServiceMeta;
    }

    protected abstract void doRegister(RegisterMeta registerMeta);

    protected abstract void doUnRegister(RegisterMeta registerMeta);

    protected abstract void doSubscribe(ServiceMeta serviceMeta);

}
