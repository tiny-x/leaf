package com.leaf.console.config;

import com.leaf.common.concurrent.ConcurrentSet;
import com.leaf.common.model.ServiceMeta;
import com.leaf.register.api.*;
import com.leaf.register.api.model.RegisterMeta;
import com.leaf.register.api.model.SubscribeMeta;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author yefei
 */
@Configuration
public class Config {

    public static ConcurrentMap<ServiceMeta, CopyOnWriteArrayList<RegisterMeta>> registerMetasMap = new ConcurrentHashMap<>();

    public static ConcurrentMap<ServiceMeta, CopyOnWriteArrayList<SubscribeMeta>> subscribeMetasMap = new ConcurrentHashMap<>();

    @Bean
    public RegisterService registerService() {
        RegisterService registerService = RegisterFactory.registerService(RegisterType.ZOOKEEPER);
        registerService.connectToRegistryServer("121.43.175.216:2181");
        List<ServiceMeta> serviceMetas = registerService.lookup();
        for (ServiceMeta serviceMeta : serviceMetas) {
            registerService.subscribeSubscribeMeta(serviceMeta, new NotifyListener<SubscribeMeta>() {
                @Override
                public void notify(SubscribeMeta subscribeMeta, NotifyEvent event) {
                    CopyOnWriteArrayList<SubscribeMeta> subscribeMetas = subscribeMetasMap.get(subscribeMeta.getServiceMeta());
                    if (subscribeMetas == null) {
                        CopyOnWriteArrayList<SubscribeMeta> newSubscribeMetas = new CopyOnWriteArrayList<>();
                        subscribeMetas =  subscribeMetasMap.putIfAbsent(serviceMeta, newSubscribeMetas);
                        if(subscribeMetas == null) {
                            subscribeMetas = newSubscribeMetas;
                        }
                    }
                    if (event == NotifyEvent.ADD) {
                        subscribeMetas.add(subscribeMeta);
                    } else {
                        subscribeMetas.remove(subscribeMeta);
                    }
                }
            });

            SubscribeMeta subscribeMeta = new SubscribeMeta(serviceMeta);
            registerService.subscribeRegisterMeta(subscribeMeta, new NotifyListener<RegisterMeta>() {
                @Override
                public void notify(RegisterMeta registerMeta, NotifyEvent event) {
                    CopyOnWriteArrayList<RegisterMeta> registerMetas = registerMetasMap.get(serviceMeta);
                    if (registerMetas == null) {
                        CopyOnWriteArrayList<RegisterMeta> newRegisterMetas = new CopyOnWriteArrayList<>();
                        registerMetas = registerMetasMap.putIfAbsent(serviceMeta, newRegisterMetas);
                        if (registerMetas == null) {
                            registerMetas = newRegisterMetas;
                        }
                    }
                    if (event == NotifyEvent.ADD) {
                        registerMetas.add(registerMeta);
                    } else {
                        registerMetas.remove(registerMeta);
                    }
                }
            });
        }

        return registerService;
    }
}
