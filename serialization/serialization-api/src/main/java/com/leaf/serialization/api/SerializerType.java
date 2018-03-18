package com.leaf.serialization.api;

public enum SerializerType {
    PROTO_STUFF ((byte) 0x00),
    HESSIAN     ((byte) 0x01),
    KRYO        ((byte) 0x02),
    JAVA        ((byte) 0x03),
    FAST_JSON   ((byte) 0x04),
    // ...
    ;

    SerializerType(byte value) {
        if (0x00 <= value && value <= 0x07) {
            this.value = value;
        } else {
            throw new IllegalArgumentException("out of range(0x00 ~ 0x07): " + value);
        }
    }

    private final byte value;

    public byte value() {
        return value;
    }

    public static SerializerType parse(String name) {
        for (SerializerType s : values()) {
            if (s.name().equalsIgnoreCase(name)) {
                return s;
            }
        }
        return null;
    }

    public static SerializerType parse(byte value) {
        for (SerializerType s : values()) {
            if (s.value() == value) {
                return s;
            }
        }
        return null;
    }

    public static SerializerType getDefault() {
        return PROTO_STUFF;
    }
}