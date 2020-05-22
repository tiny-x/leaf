package com.leaf.console.controller;

import com.google.common.collect.Lists;
import com.leaf.common.model.ServiceMeta;
import com.leaf.console.config.Config;
import com.leaf.console.model.Response;
import com.leaf.console.model.ServiceMetaExtend;
import com.leaf.register.api.RegisterService;
import com.leaf.register.api.model.RegisterMeta;
import com.leaf.register.api.model.SubscribeMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author yefei
 */
@RestController
public class ServiceController {

    @Autowired
    private RegisterService registerService;

    @RequestMapping("/services")
    public Response<List<ServiceMetaExtend>> services() {
        List<ServiceMeta> serviceMetas = registerService.lookup();

        ArrayList<ServiceMetaExtend> list = Lists.newArrayList();
        for (ServiceMeta serviceMeta : serviceMetas) {
            ServiceMetaExtend serviceMetaExtend = new ServiceMetaExtend(
                    serviceMeta.getGroup(),
                    serviceMeta.getServiceProviderName(),
                    serviceMeta.getVersion());

            CopyOnWriteArrayList<RegisterMeta> registerMetas = Config.registerMetasMap.get(serviceMeta);
            if (registerMetas == null) {
                serviceMetaExtend.setProviders(0);
            } else {
                serviceMetaExtend.setProviders(registerMetas.size());
            }
            CopyOnWriteArrayList<SubscribeMeta> subscribeMetas = Config.subscribeMetasMap.get(serviceMeta);
            if (subscribeMetas == null) {
                serviceMetaExtend.setConsumers(0);
            } else {
                serviceMetaExtend.setConsumers(subscribeMetas.size());
            }
            list.add(serviceMetaExtend);
        }
        return Response.ofSuccess(list);
    }

    @RequestMapping("/providers")
    public Response<List<RegisterMeta>> providers() {
        List<RegisterMeta> registerMetas = Lists.newArrayList();
        for (Map.Entry<ServiceMeta, CopyOnWriteArrayList<RegisterMeta>> entry : Config.registerMetasMap.entrySet()) {
            registerMetas.addAll(entry.getValue());
        }
        return Response.ofSuccess(registerMetas);
    }

    @RequestMapping("/consumers")
    public Response<List<SubscribeMeta>> consumers() {
        List<SubscribeMeta> subscribeMetas = Lists.newArrayList();
        for (Map.Entry<ServiceMeta, CopyOnWriteArrayList<SubscribeMeta>> entry : Config.subscribeMetasMap.entrySet()) {
            subscribeMetas.addAll(entry.getValue());
        }
        return Response.ofSuccess(subscribeMetas);
    }
}
