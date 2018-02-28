package com.leaf.register;

import com.leaf.common.UnresolvedAddress;
import com.leaf.common.model.RegisterMeta;
import com.leaf.common.model.ServiceMeta;
import com.leaf.common.utils.InetUtils;
import com.leaf.register.api.AbstractRegisterService;
import com.leaf.register.api.RegisterService;
import com.leaf.register.api.RegisterType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkArgument;

public class DefaultRegisterService extends AbstractRegisterService implements RegisterService {

    private ConcurrentMap<UnresolvedAddress, DefaultRegisterClient> regiters = new ConcurrentHashMap<>();

    public DefaultRegisterService() {
    }

    @Override
    public void connectToRegistryServer(String addresses) {
        UnresolvedAddress[] unresolvedAddresses = InetUtils.spiltAddress(addresses);
        for (UnresolvedAddress unresolvedAddress : unresolvedAddresses) {
            DefaultRegisterClient registerClient = new DefaultRegisterClient(unresolvedAddress, this);
            regiters.put(unresolvedAddress, registerClient);
        }
    }

    @Override
    public void doRegister(RegisterMeta registerMeta) {
        checkArgument(!regiters.isEmpty(), "not connect any registry server");

        for (Map.Entry<UnresolvedAddress, DefaultRegisterClient> register : regiters.entrySet()) {
            register.getValue().register(registerMeta);
        }
    }

    @Override
    public void doUnRegister(RegisterMeta registerMeta) {
        checkArgument(!regiters.isEmpty(), "not connect any registry server");

        for (Map.Entry<UnresolvedAddress, DefaultRegisterClient> register : regiters.entrySet()) {
            register.getValue().unRegister(registerMeta);
        }
    }

    @Override
    public void doSubscribe(ServiceMeta serviceMeta) {
        checkArgument(!regiters.isEmpty(), "not connect any registry server");

        for (Map.Entry<UnresolvedAddress, DefaultRegisterClient> register : regiters.entrySet()) {
            register.getValue().subscribe(serviceMeta);
        }
    }

    @Override
    public RegisterType registerType() {
        return RegisterType.DEFAULT;
    }
}
