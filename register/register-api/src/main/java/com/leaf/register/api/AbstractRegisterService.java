package com.leaf.register.api;

import com.leaf.common.UnresolvedAddress;
import com.leaf.common.concurrent.ConcurrentSet;
import com.leaf.common.model.ServiceMeta;
import com.leaf.register.api.model.RegisterMeta;
import com.leaf.register.api.model.SubscribeMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractRegisterService extends AbstractRetryRegisterService {

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
    private final ConcurrentSet<RegisterMeta> providerRegisterMetas = new ConcurrentSet<>();

    /**
     * 已经订阅的服务
     */
    private final ConcurrentSet<SubscribeMeta> consumersServiceMeta = new ConcurrentSet<>();

    public AbstractRegisterService() {
    }

    @Override
    public void register(RegisterMeta registerMeta) {
        logger.info("[REGISTER] register service: {}", registerMeta);
        doRegister(registerMeta);
    }

    @Override
    public void unRegister(RegisterMeta registerMeta) {
        logger.info("[UN_REGISTER] unRegister service: {}", registerMeta);
        doUnRegister(registerMeta);
        providerRegisterMetas.remove(registerMeta);
    }

    @Override
    public void subscribe(SubscribeMeta subscribeMeta, NotifyListener notifyListener) {
        logger.info("[SUBSCRIBE] subscribe service: {}", subscribeMeta);
        subscribeListeners.put(subscribeMeta.getServiceMeta(), notifyListener);
        consumersServiceMeta.add(subscribeMeta);
        doSubscribe(subscribeMeta);
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

    public void notify(ServiceMeta serviceMeta, NotifyEvent event, RegisterMeta... registerMetas) {
        logger.info("[NOTIFY] consumer service: {} notifyEvent：{}, registerMetas: {}",
                serviceMeta,
                event.name(),
                registerMetas);

        if (registerMetas != null && registerMetas.length > 0) {
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

    public ConcurrentSet<SubscribeMeta> getConsumersServiceMetas() {
        return consumersServiceMeta;
    }

}
