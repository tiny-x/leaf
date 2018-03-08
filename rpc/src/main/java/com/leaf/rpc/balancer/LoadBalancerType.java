package com.leaf.rpc.balancer;

public enum LoadBalancerType {

    RANDOM,
    ROUND_ROBIN;

    public static LoadBalancerType parse(String name) {
        for (LoadBalancerType loadBalancerType : LoadBalancerType.values()) {
            if (loadBalancerType.name().equals(name)) {
                return loadBalancerType;
            }
        }
        return null;
    }
}
