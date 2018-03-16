package com.leaf.register.api;

public enum RegisterType {

    DEFAULT,
    ZOOKEEPER;

    public static RegisterType parse(String registerType) {
        RegisterType[] registerTypes = RegisterType.values();
        for (RegisterType type : registerTypes) {
            if (type.name().equals(registerType)) {
                return type;
            }
        }
        return null;
    }
}
