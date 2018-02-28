package com.leaf.register.api;


import com.leaf.common.UnresolvedAddress;
import com.leaf.common.model.RegisterMeta;
import com.leaf.common.model.ServiceMeta;

import java.util.List;

public interface RegisterService {

    void register(RegisterMeta registerMeta);

    void unRegister(RegisterMeta RegisterMeta);

    void subscribe(ServiceMeta serviceMeta, NotifyListener notifyListener);

    List<RegisterMeta> lookup(RegisterMeta RegisterMeta);

    void offlineListening(UnresolvedAddress address, OfflineListener listener);

    RegisterType registerType();

    void connectToRegistryServer(String addresses);

}
