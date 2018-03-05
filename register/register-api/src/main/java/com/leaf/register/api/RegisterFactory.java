package com.leaf.register.api;

import java.util.Iterator;
import java.util.ServiceLoader;

public class RegisterFactory {

    public static RegisterService registerService(RegisterType registerType) {
        ServiceLoader<RegisterService> load = ServiceLoader.load(RegisterService.class);
        Iterator<RegisterService> iterator = load.iterator();
        while (iterator.hasNext()) {
            RegisterService registerService = iterator.next();
            if (registerService.registerType() == registerType){
                return registerService;
            }
        }
        return null;
    }
}
