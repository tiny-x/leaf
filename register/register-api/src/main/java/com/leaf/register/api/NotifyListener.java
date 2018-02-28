package com.leaf.register.api;


import com.leaf.common.model.RegisterMeta;

public interface NotifyListener {

    void notify(RegisterMeta registerMeta, NotifyEvent event);
}
