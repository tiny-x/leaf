package com.leaf.rpc.controller;

import com.google.common.util.concurrent.RateLimiter;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 令牌桶
 */
public class RateLimitFlowController implements FlowController {

    private RateLimiter rateLimiter;

    private long qps;

    public RateLimitFlowController(long qps) {
        rateLimiter = RateLimiter.create(qps);
        this.rateLimiter = rateLimiter;
        this.qps = qps;
    }

    @Override
    public void flowController() throws RejectedExecutionException {
        if (!rateLimiter.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
            String message = String.format("CounterFlowController rate:[%d]", rateLimiter.getRate());
            throw new RejectedExecutionException(message);
        }
    }
}
