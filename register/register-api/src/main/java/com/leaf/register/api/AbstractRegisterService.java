package com.leaf.register.api;

import com.google.common.collect.Lists;
import com.leaf.common.UnresolvedAddress;
import com.leaf.common.concurrent.ConcurrentSet;
import com.leaf.common.model.ServiceMeta;
import com.leaf.common.utils.Collections;
import com.leaf.register.api.model.RegisterMeta;
import com.leaf.register.api.model.SubscribeMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractRegisterService implements RegisterService {

    private final static Logger logger = LoggerFactory.getLogger(AbstractRegisterService.class);

    /**
     * 订阅provider监听器
     */
    private final ConcurrentMap<ServiceMeta, CopyOnWriteArrayList<NotifyListener<RegisterMeta>>> subscribeProviderListeners = new ConcurrentHashMap<>();

    /**
     * 订阅consumer监听器
     */
    private final ConcurrentMap<ServiceMeta, CopyOnWriteArrayList<NotifyListener<SubscribeMeta>>> subscribeSubscriberListeners = new ConcurrentHashMap<>();

    /**
     * 服务下线通知监听
     */
    private final ConcurrentMap<UnresolvedAddress, CopyOnWriteArrayList<OfflineListener>> offlineListeners = new ConcurrentHashMap<>();

    /**
     * 注册者
     */
    protected final ConcurrentSet<RegisterMeta> registers = new ConcurrentSet<>();

    /**
     * 订阅provider的客户端
     */
    protected final ConcurrentSet<SubscribeMeta> subscribes = new ConcurrentSet<>();

    /**
     * 订阅consumer的客户端（控制台等需要）
     */
    protected final ConcurrentSet<ServiceMeta> subscribeConsumers = new ConcurrentSet<>();

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
        registers.remove(registerMeta);
    }

    @Override
    public void subscribeRegisterMeta(SubscribeMeta subscribeMeta, NotifyListener<RegisterMeta> notifyListener) {
        logger.info("[SUBSCRIBE] subscribe register meta: {}", subscribeMeta);
        subscribes.add(subscribeMeta);

        ServiceMeta serviceMeta = subscribeMeta.getServiceMeta();
        CopyOnWriteArrayList<NotifyListener<RegisterMeta>> notifyListeners = subscribeProviderListeners.get(serviceMeta);
        if (notifyListeners == null) {
            CopyOnWriteArrayList<NotifyListener<RegisterMeta>> newNotifyListeners = new CopyOnWriteArrayList();
            notifyListeners = subscribeProviderListeners.putIfAbsent(serviceMeta, newNotifyListeners);
            if (notifyListeners == null) {
                notifyListeners = newNotifyListeners;
            }
        }
        notifyListeners.add(notifyListener);
        doSubscribeRegisterMeta(subscribeMeta);
    }

    @Override
    public void subscribeSubscribeMeta(ServiceMeta serviceMeta, NotifyListener<SubscribeMeta> notifyListener) {
        logger.info("[SUBSCRIBE] subscribe subscriber meta: {}", serviceMeta);
        subscribeConsumers.add(serviceMeta);

        CopyOnWriteArrayList<NotifyListener<SubscribeMeta>> notifyListeners = subscribeSubscriberListeners.get(serviceMeta);
        if (notifyListeners == null) {
            CopyOnWriteArrayList<NotifyListener<SubscribeMeta>> newNotifyListeners = new CopyOnWriteArrayList<>();
            notifyListeners = subscribeSubscriberListeners.putIfAbsent(serviceMeta, newNotifyListeners);
            if (notifyListeners == null) {
                notifyListeners = newNotifyListeners;
            }
        }
        notifyListeners.add(notifyListener);
        doSubscribeSubscribeMeta(serviceMeta);
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
        if (Collections.isNotEmpty(offlineListenerList)) {
            for (OfflineListener offlineListener : offlineListenerList) {
                offlineListener.offline();
            }
        }
    }

    public void notify(NotifyEvent event, RegisterMeta... registerMetas) {
        logger.info("[NOTIFY] client registerMeta: {}, notifyEvent：{}", registerMetas, event.name());
        if (Collections.isNotEmpty(registerMetas)) {
            for (RegisterMeta meta : registerMetas) {
                CopyOnWriteArrayList<NotifyListener<RegisterMeta>> notifyListeners = subscribeProviderListeners.get(meta.getServiceMeta());
                for (NotifyListener<RegisterMeta> notifyListener : notifyListeners) {
                    notifyListener.notify(meta, event);
                }
            }
        }

    }

    public void notify(NotifyEvent event, SubscribeMeta... subscribeMetas) {
        logger.info("[NOTIFY] client subscribeMeta: {}, notifyEvent：{}", subscribeMetas, event.name());
        if (Collections.isNotEmpty(subscribeMetas)) {
            for (SubscribeMeta meta : subscribeMetas) {
                CopyOnWriteArrayList<NotifyListener<SubscribeMeta>> notifyListeners = subscribeSubscriberListeners.get(meta.getServiceMeta());
                for (NotifyListener<SubscribeMeta> notifyListener : notifyListeners) {
                    notifyListener.notify(meta, event);
                }
            }
        }
    }

    public List<RegisterMeta> getRegisterMetas() {
        List<RegisterMeta> registerMetas = Lists.newArrayList(registers);
        return registerMetas;
    }

    public List<SubscribeMeta> getServiceMetas() {
        List<SubscribeMeta> subscribeMetas = Lists.newArrayList(subscribes);
        return subscribeMetas;
    }

    protected abstract void doRegister(RegisterMeta registerMeta);

    protected abstract void doUnRegister(RegisterMeta registerMeta);

    protected abstract void doSubscribeRegisterMeta(SubscribeMeta serviceMeta);

    protected abstract void doSubscribeSubscribeMeta(ServiceMeta serviceMeta);

}
