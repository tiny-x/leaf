package com.leaf.common.model;

/**
 * 服务目录: <服务组别, 服务名称, 服务版本号>
 */
public abstract class Directory {

    private String directoryCache;

    /** 服务所属组别 */
    public abstract String getGroup();

    /** 服务名称 */
    public abstract String getServiceProviderName();

    /** 服务版本号 */
    public abstract String getVersion();

    public String directory() {
        if (directoryCache != null) {
            return directoryCache;
        }

        StringBuilder buf = new StringBuilder();
        buf.append(getGroup())
                .append('-')
                .append(getServiceProviderName())
                .append('-')
                .append(getVersion());

        directoryCache = buf.toString();

        return directoryCache;
    }

    public void clear() {
        directoryCache = null;
    }
}