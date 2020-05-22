package com.leaf.register.api.model;

import com.leaf.common.UnresolvedAddress;
import com.leaf.common.model.ServiceMeta;
import com.leaf.register.api.NotifyEvent;

public class Message {

    private UnresolvedAddress address;

    private NotifyEvent event;

    private ServiceMeta serviceMeta;

    private RegisterMeta[] registerMetas;

    public Message(UnresolvedAddress address) {
        this.address = address;
    }

    public Message(NotifyEvent event, ServiceMeta serviceMeta, RegisterMeta... registerMetas) {
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

    public RegisterMeta[] getRegisterMetas() {
        return registerMetas;
    }

}
