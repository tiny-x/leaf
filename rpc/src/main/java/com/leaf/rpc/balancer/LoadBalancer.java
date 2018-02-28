package com.leaf.rpc.balancer;

import com.leaf.common.model.Directory;
import com.leaf.remoting.api.channel.ChannelGroup;

import java.util.concurrent.CopyOnWriteArrayList;


/**
 * 软负载均衡
 */
public interface LoadBalancer {

    ChannelGroup select(CopyOnWriteArrayList<ChannelGroup> list, Directory directory);
}
