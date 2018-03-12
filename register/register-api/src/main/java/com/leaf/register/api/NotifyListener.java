package com.leaf.register.api;


import com.leaf.register.api.model.RegisterMeta;

public interface NotifyListener {

    void notify(RegisterMeta registerMeta, NotifyEvent event);
}
