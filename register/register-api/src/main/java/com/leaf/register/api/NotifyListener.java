package com.leaf.register.api;

public interface NotifyListener<T> {

    void notify(T t, NotifyEvent event);
}
