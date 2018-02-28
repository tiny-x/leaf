package com.leaf.example.load.balancer;

import com.leaf.example.load.Channel;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 加权随机
 */
public class RandomLoadBalancer {

    public static Channel select(Channel... channels) {

        if (channels.length == 1) {
            return channels[0];
        }

        boolean sameWeight = true;

        for (int i = 1; i < channels.length && sameWeight; i++) {
            sameWeight = (channels[0].getWeight() == channels[i].getWeight());
        }

        int sumWeight = 0;
        for (int i = 0; i < channels.length; i++) {
            sumWeight += channels[i].getWeight();
        }

        Random random = ThreadLocalRandom.current();

        if (sameWeight) {
            return channels[random.nextInt(channels.length)];
        } else {
            int offset = random.nextInt(sumWeight);
            for (Channel channel : channels) {
                offset -= channel.getWeight();
                if (offset < 0) {
                    return channel;
                }
            }
            return null;
        }
    }

    public static void main(String[] args) {
        Channel[] channels = new Channel[5];
        channels[0] = new Channel("A", 30);
        channels[1] = new Channel("B", 60);
        channels[2] = new Channel("C", 30);
        channels[3] = new Channel("D", 10);
        channels[4] = new Channel("E", 20);

        Map<String, Integer> count = new HashMap<>(5);
        for (int i = 0; i < 100000; i++) {
            Channel select = select(channels);
            if (count.containsKey(select.getName())) {
                count.put(select.getName(), count.get(select.getName()) + 1);
            } else {
                count.put(select.getName(), 1);
            }
        }

        System.out.println(count);
    }
}
