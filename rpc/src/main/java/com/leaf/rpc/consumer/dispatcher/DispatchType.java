package com.leaf.rpc.consumer.dispatcher;

public enum DispatchType {
    ROUND,      // 单播
    BROADCAST;  // 广播

    public static DispatchType parse(String name) {
        for (DispatchType s : values()) {
            if (s.name().equalsIgnoreCase(name)) {
                return s;
            }
        }
        return null;
    }

    public static DispatchType getDefault() {
        return ROUND;
    }
}