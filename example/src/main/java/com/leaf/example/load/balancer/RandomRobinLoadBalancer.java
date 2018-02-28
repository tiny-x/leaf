package com.leaf.example.load.balancer;


import com.leaf.example.load.Channel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class RandomRobinLoadBalancer {

    private AtomicInteger integer = new AtomicInteger(0);

    public Channel select(Channel... channels) {

        if (channels.length == 1) {
            return channels[0];
        }
        boolean sameWeight = true;
        for (int i = 1; i < channels.length && sameWeight; i++) {
            sameWeight = (channels[0].getWeight() == channels[i].getWeight());
        }

        int index = integer.getAndIncrement() & Integer.MAX_VALUE;
        if (sameWeight) {
            return channels[index % channels.length];
        } else {
            int commonDivisor = commonDivisor(channels);
            int sumWeight = 0;
            for (int i = 0; i < channels.length; i++) {
                sumWeight += channels[i].getWeight();
            }
            // sumWeight / commonDivisor --》一轮多少次
            int offset = index % (sumWeight / commonDivisor);
            int weight = 0;
            for (int i = 0; i < channels.length; i++) {
                weight += channels[i].getWeight();
                if (offset * commonDivisor < weight) {
                    return channels[i];
                }
            }
        }
        return channels[index % channels.length];
    }

    // 求最大公约数
    private int commonDivisor(Channel... channels) {
        int commonDivisor = gcd(channels[0].getWeight(), channels[1].getWeight());
        for (int i = 1; i < channels.length - 1; i++) {
            commonDivisor = gcd(commonDivisor, channels[i].getWeight());
        }
        return commonDivisor;
    }

    private int gcd(int a, int b) {
        if (a < b) {
            a = a ^ b;
            b = b ^ a;
            a = a ^ b;
        }
        if (a % b == 0)
            return b;
        else
            return gcd(b, a % b);
    }

    public static void main(String[] args) {
        RandomRobinLoadBalancer balancer = new RandomRobinLoadBalancer();
        Channel[] channels = new Channel[5];
        channels[0] = new Channel("A", 30);
        channels[1] = new Channel("B", 60);
        channels[2] = new Channel("C", 30);
        channels[3] = new Channel("D", 10);
        channels[4] = new Channel("E", 20);

        Map<String, Integer> count = new HashMap<>(5);
        for (int i = 0; i < 10000; i++) {
            Channel select = balancer.select(channels);
            if (count.containsKey(select.getName())) {
                count.put(select.getName(), count.get(select.getName()) + 1);
            } else {
                count.put(select.getName(), 1);
            }
        }
        System.out.println(count);
    }
}
