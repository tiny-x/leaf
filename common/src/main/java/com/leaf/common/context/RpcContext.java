package com.leaf.common.context;

import java.util.HashMap;
import java.util.Map;

public class RpcContext {

    private final static ThreadLocal<Map<String, String>> attachment = ThreadLocal.withInitial(() -> new HashMap());

    public static Map<String, String> getAttachments() {
        return attachment.get();
    }

    public static void setAttachments(Map<String, String> map) {
        attachment.set(map);
    }

    public static String getAttachment(String key) {
        return attachment.get().get(key);
    }

    public static void putAttachment(String key, String value) {
        attachment.get().put(key, value);
    }

    public static void clearAttachments() {
        if (attachment.get() != null) {
            attachment.get().clear();
        }
    }

}
