package com.leaf.remoting;

import com.leaf.remoting.api.ProtocolHead;
import com.leaf.common.UnresolvedAddress;
import com.leaf.remoting.api.RemotingCommandFactory;
import com.leaf.remoting.api.RequestProcessor;
import com.leaf.remoting.api.RemotingClient;
import com.leaf.remoting.api.RemotingServer;
import com.leaf.remoting.api.payload.RequestCommand;
import com.leaf.remoting.api.payload.ResponseCommand;
import com.leaf.remoting.netty.NettyClient;
import com.leaf.remoting.netty.NettyClientConfig;
import com.leaf.remoting.netty.NettyServer;
import com.leaf.remoting.netty.NettyServerConfig;
import com.leaf.serialization.api.SerializerType;
import io.netty.channel.ChannelHandlerContext;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

public class RemotingServerTest {

    RemotingServer rpcServer;
    RemotingClient rpcClient;

    @Before
    public void before() {
        rpcServer = new NettyServer(new NettyServerConfig());
        rpcClient = new NettyClient(new NettyClientConfig());
        rpcServer.start();
        rpcClient.start();
    }

    @Test
    public void testInvokeSync() throws Exception {
        rpcServer.registerRequestProcess(new RequestProcessor() {
            @Override
            public ResponseCommand process(ChannelHandlerContext context, RequestCommand request) {
                String info = "hi client";
                System.out.printf("------- > receive client message: %s\n", new String(request.getBody()));

                ResponseCommand response = RemotingCommandFactory.createResponseCommand(
                        request.getSerializerCode(),
                        info.getBytes(),
                        request.getInvokeId()
                );

                return response;
            }

            @Override
            public boolean rejectRequest() {
                return false;
            }
        }, Executors.newCachedThreadPool());

        RequestCommand request = new RequestCommand(ProtocolHead.RPC_REQUEST, SerializerType.PROTO_STUFF.value(), "hello register".getBytes());
        UnresolvedAddress address = new UnresolvedAddress("127.0.0.1", 9180);
        rpcClient.connect(address);

        ResponseCommand response = rpcClient.invokeSync(address,
                request,
                3000L
        );
        System.out.printf("------- > receive register message: %s\n", new String(response.getBody()));
    }

    @Test
    public void testInvokeAsync() throws Exception {
        rpcServer.registerRequestProcess(new RequestProcessor() {
            @Override
            public ResponseCommand process(ChannelHandlerContext context, RequestCommand request) {
                String info = "hi client";
                System.out.printf("------- > receive client message: %s\n", new String(request.getBody()));

                ResponseCommand response = RemotingCommandFactory.createResponseCommand(
                        request.getSerializerCode(),
                        info.getBytes(),
                        request.getInvokeId()
                );
                return response;
            }

            @Override
            public boolean rejectRequest() {
                return false;
            }
        }, Executors.newCachedThreadPool());

        RequestCommand request = new RequestCommand(ProtocolHead.RPC_REQUEST, SerializerType.PROTO_STUFF.value(), "hello register".getBytes());

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        UnresolvedAddress address = new UnresolvedAddress("127.0.0.1", 9180);
        rpcClient.connect(address);

        rpcClient.invokeAsync(address,
                request,
                3000L,
                (future) -> {
                    ResponseCommand response = null;
                    try {
                        response = future.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.printf("------- > receive register message: %s\n", new String(response.getBody()));
                    //countDownLatch.countDown();
                }
        );
        System.out.println("------> 异步执行！");
        countDownLatch.await();
    }
}
