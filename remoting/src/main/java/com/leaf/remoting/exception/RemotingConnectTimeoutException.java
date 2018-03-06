
package com.leaf.remoting.exception;

public class RemotingConnectTimeoutException extends RemotingConnectException {
    private static final long serialVersionUID = -5565366231695911316L;

    public RemotingConnectTimeoutException(String addr) {
        this(addr, null);
    }

    public RemotingConnectTimeoutException(String addr, Throwable cause) {
        super("connect to <" + addr + "> timeout", cause);
    }
}
