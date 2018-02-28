package com.leaf.register.api;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

public class RegisterFactory {

    private static final Map<String, RegisterService> REGISTER_SERVER_MAP = new HashMap();

    static {
        ServiceLoader<RegisterService> load = ServiceLoader.load(RegisterService.class);
        Iterator<RegisterService> iterator = load.iterator();
        while (iterator.hasNext()) {
            RegisterService registerService = iterator.next();
            REGISTER_SERVER_MAP.put(registerService.registerType().name(), registerService);
        }
    }

    public static RegisterService registerService(RegisterType registerType) {
        RegisterService registerService = REGISTER_SERVER_MAP.get(registerType.name());
        if (registerService == null) {
            throw new IllegalArgumentException("Illegal serializerType : " + registerType);
        } else {
            return registerService;
        }
    }
}
