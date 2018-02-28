package com.leaf.rpc.consumer;

import com.leaf.rpc.consumer.cluster.ClusterInvoker;

public class StrategyConfig {

    private ClusterInvoker.Strategy strategy;

    private int retries;

    public StrategyConfig(ClusterInvoker.Strategy strategy) {
        this(strategy, 0);
    }

    public StrategyConfig(ClusterInvoker.Strategy strategy, int retries) {
        this.strategy = strategy;
        this.retries = retries;
    }

    public ClusterInvoker.Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(ClusterInvoker.Strategy strategy) {
        this.strategy = strategy;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }
}
