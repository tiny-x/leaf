package com.leaf.register.api.model;

import com.leaf.common.UnresolvedAddress;
import com.leaf.common.model.ServiceMeta;
import com.leaf.register.api.NotifyEvent;

import java.util.List;

public class Notify {

    private UnresolvedAddress address;

    private NotifyEvent event;

    private ServiceMeta serviceMeta;

    private List<RegisterMeta> registerMetas;

    public Notify(UnresolvedAddress address) {
        this.address = address;
    }

    public Notify(NotifyEvent event, ServiceMeta serviceMeta, List<RegisterMeta> registerMetas) {
        this.event = event;
        this.serviceMeta = serviceMeta;
        this.registerMetas = registerMetas;
    }

    public NotifyEvent getEvent() {
        return event;
    }


    public ServiceMeta getServiceMeta() {
        return serviceMeta;
    }

    public UnresolvedAddress getAddress() {
        return address;
    }

    public List<RegisterMeta> getRegisterMetas() {
        return registerMetas;
    }

}
