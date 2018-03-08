package com.leaf.rpc.consumer;


public enum  InvokeType {

    SYNC,
    ASYNC,
    ONE_WAY;

    public static InvokeType parse(String name) {
        for (InvokeType invokeType : InvokeType.values()) {
            if (invokeType.name().equals(name)) {
                return invokeType;
            }
        }
        return null;
    }
}
