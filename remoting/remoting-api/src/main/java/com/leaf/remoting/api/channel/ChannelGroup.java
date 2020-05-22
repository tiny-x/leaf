package com.leaf.remoting.api.channel;

import com.leaf.common.UnresolvedAddress;
import com.leaf.common.model.Directory;
import io.netty.channel.Channel;

/**
 * 点对点 channel, 比如server与client 建立4条连接，就是一个ChannelGroup对象
 * ChannelGroup 还维护 服务 与 权重的关系
 *
 * @author yefei
 */
public interface ChannelGroup {

    /**
     * 连接的地址
     *
     * @return
     */
    UnresolvedAddress remoteAddress();

    /**
     *
     * @return
     */
    Channel next();

    /**
     * add channel
     *
     * @param channel
     * @return
     */
    boolean addChannel(Channel channel);

    /**
     *
     * @param channel
     * @return
     */
    boolean removeChannel(Channel channel);

    /**
     *
     * @param directory
     * @param weight
     */
    void setWeight(Directory directory, int weight);

    /**
     *
     * @param directory
     * @return
     */
    int getWeight(Directory directory);

    /**
     *
     * @param directory
     */
    void removeWeight(Directory directory);

    /**
     *
     * @return
     */
    boolean isAvailable();

    /**
     *
     * @return
     */
    int size();
}
