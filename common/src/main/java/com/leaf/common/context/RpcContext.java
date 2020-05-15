package com.leaf.common.context;

import java.util.HashMap;
import java.util.Map;

/**
 * rpc 上下文
 *
 * @author yefei
 */
public class RpcContext {

    private final static ThreadLocal<Map<String, String>> attachment = ThreadLocal.withInitial(() -> new HashMap());

    private final static ThreadLocal<Long> timeouts = ThreadLocal.withInitial(() -> 0L);

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

    /**
     * 可设置每次调用的超时时间
     *
     * @param timeout
     */
    public static void setTimeout(Long timeout) {
        timeouts.set(timeout);
    }

    public static Long getTimeout() {
        return timeouts.get();
    }

    public static void resetTimeout() {
        timeouts.set(0L);
    }
}
