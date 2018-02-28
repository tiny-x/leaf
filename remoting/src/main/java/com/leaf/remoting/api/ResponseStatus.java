package com.leaf.remoting.api;

public enum ResponseStatus {

    SUCCESS((byte) 0x01, "SUCCESS"),
    CLIENT_TIME_OUT((byte) 0x02, "CLIENT_TIME_OUT"),
    SERVER_TIME_OUT((byte) 0x03, "SERVER_TIME_OUT"),
    CLIENT_ERROR((byte) 0x04, "CLIENT_ERROR"),
    SERVER_ERROR((byte) 0x05, "SERVER_ERROR"),
    SYSTEM_BUSY((byte) 0x06, "SYSTEM_BUSY"),

    FLOW_CONTROL((byte) 0x08, "FLOW_CONTROL"),
    SERVICE_NOT_FOUND((byte) 0x09, "GLOBAL_FLOW_CONTROL"),
    SEND_REQUEST_ERROR((byte) 0x10, "GLOBAL_FLOW_CONTROL")
    ;

    private byte value;

    private String desc;

    public byte value() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    ResponseStatus(byte value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
