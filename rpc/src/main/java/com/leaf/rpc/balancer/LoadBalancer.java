package com.leaf.rpc.balancer;

import com.leaf.common.model.Directory;
import com.leaf.remoting.api.channel.ChannelGroup;

import java.util.List;


/**
 * 软负载均衡
 * @author yefei
 */
public interface LoadBalancer {

    /**
     * select
     * @param list
     * @param directory
     * @return
     */
    ChannelGroup select(List<ChannelGroup> list, Directory directory);
}
