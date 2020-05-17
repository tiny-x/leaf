package com.leaf.common.utils;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemPropertiesUtils {

    private static final Logger logger = LoggerFactory.getLogger(SystemPropertiesUtils.class);

    public static String getPropertiesValue(String key) {
        if (Strings.isNullOrEmpty(key)) {
            throw new RuntimeException("key not be null!");
        }
        return System.getProperty(key);
    }

    public static String getEnvValue(String key) {
        if (Strings.isNullOrEmpty(key)) {
            throw new RuntimeException("key not be null!");
        }
        return System.getenv(key);
    }

    /**
     * 优先取环境变量
     *
     * @param key
     * @return
     */
    public static String getEnvOrPropertiesValue(String key) {
        if (Strings.isNullOrEmpty(key)) {
            throw new RuntimeException("key not be null!");
        }
        String envValue = getEnvValue(key);
        if (Strings.isNullOrEmpty(envValue)) {
            return getPropertiesValue(key);
        }
        return envValue;
    }

}
