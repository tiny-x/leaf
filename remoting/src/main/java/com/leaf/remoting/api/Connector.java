package com.leaf.remoting.api;

import com.leaf.common.UnresolvedAddress;

public class Connector {

    private UnresolvedAddress address;

    private volatile boolean needReconnect = true;

    public Connector(UnresolvedAddress address) {
        this.address = address;
    }

    public UnresolvedAddress getAddress() {
        return address;
    }

    public void setNeedReconnect(boolean needReconnect) {
        this.needReconnect = needReconnect;
    }

    public boolean isNeedReconnect() {
        return needReconnect;
    }
}
