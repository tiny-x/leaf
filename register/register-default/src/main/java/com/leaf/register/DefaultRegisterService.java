package com.leaf.register;

import com.leaf.common.UnresolvedAddress;
import com.leaf.common.model.ServiceMeta;
import com.leaf.common.utils.InetUtils;
import com.leaf.register.api.AbstractRegisterService;
import com.leaf.register.api.RegisterService;
import com.leaf.register.api.RegisterType;
import com.leaf.register.api.model.RegisterMeta;
import com.leaf.register.api.model.SubscribeMeta;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author yefei
 */
public class DefaultRegisterService extends AbstractRegisterService implements RegisterService {

    private ConcurrentMap<UnresolvedAddress, DefaultRegisterClient> registerClients = new ConcurrentHashMap<>();

    private String namespace;

    public DefaultRegisterService() {
    }

    @Override
    public List<ServiceMeta> lookup() {
        return null;
    }

    @Override
    protected void doSubscribeSubscribeMeta(ServiceMeta serviceMeta) {
        // TODO
    }

    @Override
    public void setNamespace(String namespace) {
    }

    @Override
    public void connectToRegistryServer(String addresses) {
        UnresolvedAddress[] unresolvedAddresses = InetUtils.spiltAddress(addresses);
        for (UnresolvedAddress unresolvedAddress : unresolvedAddresses) {

            DefaultRegisterClient registerClient = registerClients.get(unresolvedAddress);
            if (registerClient == null) {
                DefaultRegisterClient newRegisterClient = new DefaultRegisterClient(this);
                registerClient = registerClients.put(unresolvedAddress, newRegisterClient);
                if (registerClient == null) {
                    registerClient = newRegisterClient;
                    registerClient.connect(unresolvedAddress);
                } else {
                    newRegisterClient.shutdownGracefully();
                }
            }
        }
    }

    @Override
    public void doRegister(RegisterMeta registerMeta) {
        checkArgument(!registerClients.isEmpty(), "not connect any registry server");

        for (Map.Entry<UnresolvedAddress, DefaultRegisterClient> register : registerClients.entrySet()) {
            register.getValue().register(registerMeta);
        }
        registers.add(registerMeta);
    }

    @Override
    public void doUnRegister(RegisterMeta registerMeta) {
        checkArgument(!registerClients.isEmpty(), "not connect any registry server");

        for (Map.Entry<UnresolvedAddress, DefaultRegisterClient> register : registerClients.entrySet()) {
            register.getValue().unRegister(registerMeta);
        }
    }

    @Override
    public void doSubscribeRegisterMeta(SubscribeMeta subscribeMeta) {
        checkArgument(!registerClients.isEmpty(), "not connect any registry server");

        for (Map.Entry<UnresolvedAddress, DefaultRegisterClient> register : registerClients.entrySet()) {
            register.getValue().subscribe(subscribeMeta);
        }
    }

    @Override
    public RegisterType registerType() {
        return RegisterType.DEFAULT;
    }

    @Override
    public void shutdown() {

    }
}
