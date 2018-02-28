package com.leaf.remoting.api.channel;

import com.leaf.common.UnresolvedAddress;
import com.leaf.common.model.Directory;
import io.netty.channel.Channel;

/**
 * 同一 点对点 channel
 *
 */
public interface ChannelGroup {

    UnresolvedAddress remoteAddress();

    Channel next();

    boolean addChannel(Channel channel);

    boolean removeChannel(Channel channel);

    void setWeight(Directory directory, int weight);

    int getWeight(Directory directory);

    void removeWeight(Directory directory);

    boolean isAvailable();

    int size();
}
