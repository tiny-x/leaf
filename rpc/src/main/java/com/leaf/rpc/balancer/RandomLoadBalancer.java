package com.leaf.rpc.balancer;

import com.leaf.common.model.Directory;
import com.leaf.common.utils.Collections;
import com.leaf.remoting.api.channel.ChannelGroup;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 加权随机
 * @author yefei
 */
public class RandomLoadBalancer implements LoadBalancer {

    private RandomLoadBalancer() {}

    private static class InnerSingleton {
        static final RandomLoadBalancer RANDOM_LOAD_BALANCER = new RandomLoadBalancer();
    }

    public static RandomLoadBalancer instance() {
        return InnerSingleton.RANDOM_LOAD_BALANCER;
    }

    @Override
    public ChannelGroup select(List<ChannelGroup> list, Directory directory) {
        if (Collections.isEmpty(list)) {
            return null;
        }
        ChannelGroup[] channelGroups = new ChannelGroup[list.size()];
        list.toArray(channelGroups);

        if (channelGroups.length == 1) {
            return channelGroups[0];
        }

        boolean sameWeight = true;

        for (int i = 1; i < channelGroups.length && sameWeight; i++) {
            sameWeight = (channelGroups[0].getWeight(directory) == channelGroups[i].getWeight(directory));
        }

        int sumWeight = 0;
        for (int i = 0; i < channelGroups.length; i++) {
            sumWeight += channelGroups[i].getWeight(directory);
        }

        Random random = ThreadLocalRandom.current();

        if (sameWeight) {
            return channelGroups[random.nextInt(channelGroups.length)];
        } else {
            int offset = random.nextInt(sumWeight);
            for (ChannelGroup channelGroup : channelGroups) {
                offset -= channelGroup.getWeight(directory);
                if (offset < 0) {
                    return channelGroup;
                }
            }
        }
        return channelGroups[random.nextInt(channelGroups.length)];
    }
    
}
