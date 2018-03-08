package com.leaf.rpc.balancer;

public class LoadBalancerFactory {

    private LoadBalancerFactory() {
    }

    public static LoadBalancer instance(LoadBalancerType loadBalancerType) {
        if (loadBalancerType == LoadBalancerType.RANDOM) {
            return RandomLoadBalancer.instance();
        } else if (loadBalancerType == LoadBalancerType.ROUND_ROBIN) {
            return RoundRobinLoadBalancer.instance();
        }
        return RandomLoadBalancer.instance();
    }
}
