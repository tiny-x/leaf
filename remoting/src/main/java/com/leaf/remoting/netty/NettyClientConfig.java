package com.leaf.remoting.netty;

public class NettyClientConfig {

    private int clientOnewaySemaphoreValue = NettySystemConfig.CLIENT_ONEWAY_SEMAPHORE_VALUE;
    private int clientAsyncSemaphoreValue = NettySystemConfig.CLIENT_ASYNC_SEMAPHORE_VALUE;
    private int clientSocketSndBufSize = NettySystemConfig.socketSndbufSize;
    private int clientSocketRcvBufSize = NettySystemConfig.socketRcvbufSize;

    private int idleAllSeconds = NettySystemConfig.IO_IDLE_ALL_TIME_SECONDS;

    private long connectTimeoutMillis = 3000L;

    private long invokeTimeoutMillis = 3000L;

    public long getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis(long connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    public long getInvokeTimeoutMillis() {
        return invokeTimeoutMillis;
    }

    public void setInvokeTimeoutMillis(long invokeTimeoutMillis) {
        this.invokeTimeoutMillis = invokeTimeoutMillis;
    }

    public int getClientOnewaySemaphoreValue() {
        return clientOnewaySemaphoreValue;
    }

    public void setClientOnewaySemaphoreValue(int clientOnewaySemaphoreValue) {
        this.clientOnewaySemaphoreValue = clientOnewaySemaphoreValue;
    }

    public int getClientAsyncSemaphoreValue() {
        return clientAsyncSemaphoreValue;
    }

    public void setClientAsyncSemaphoreValue(int clientAsyncSemaphoreValue) {
        this.clientAsyncSemaphoreValue = clientAsyncSemaphoreValue;
    }

    public int getIdleAllSeconds() {
        return idleAllSeconds;
    }

    public void setIdleAllSeconds(int idleAllSeconds) {
        this.idleAllSeconds = idleAllSeconds;
    }

    public int getClientSocketSndBufSize() {
        return clientSocketSndBufSize;
    }

    public void setClientSocketSndBufSize(int clientSocketSndBufSize) {
        this.clientSocketSndBufSize = clientSocketSndBufSize;
    }

    public int getClientSocketRcvBufSize() {
        return clientSocketRcvBufSize;
    }

    public void setClientSocketRcvBufSize(int clientSocketRcvBufSize) {
        this.clientSocketRcvBufSize = clientSocketRcvBufSize;
    }
}
