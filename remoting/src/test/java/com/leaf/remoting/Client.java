package com.leaf.remoting;

import com.leaf.common.UnresolvedAddress;
import com.leaf.remoting.api.RpcClient;
import com.leaf.remoting.exception.RemotingConnectException;
import com.leaf.remoting.netty.NettyClient;
import com.leaf.remoting.netty.NettyClientConfig;

public class Client {

    public static void main(String[] args) throws RemotingConnectException, InterruptedException {
        RpcClient client = new NettyClient(new NettyClientConfig());
        client.start();
        client.connect(new UnresolvedAddress("127.0.0.1", 9180));
    }
}
