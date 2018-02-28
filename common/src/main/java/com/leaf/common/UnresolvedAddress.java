package com.leaf.common;

/**
 * The type Unresolved address.
 *
 * @author yefei
 * @date 2017 -6-26 11:08:57
 */
public class UnresolvedAddress {

    private final String host;
    private final int port;

    /**
     * Instantiates a new Unresolved address.
     *
     * @param host the host
     * @param port the port
     */
    public UnresolvedAddress(String host, int port) {
        if (host == null || "".equals(host)) {
            throw new NullPointerException("host is null!");
        }
        if (port < 0 || port > 0xFFFF) {
            throw new IllegalArgumentException("port out of range:" + port);
        }

        this.host = host;
        this.port = port;
    }

    /**
     * Gets host.
     *
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * Gets port.
     *
     * @return the port
     */
    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        UnresolvedAddress that = (UnresolvedAddress) o;

        return port == that.port && host.equals(that.host);
    }

    @Override
    public int hashCode() {
        int result = host.hashCode();
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return host + ':' + port;
    }
}
