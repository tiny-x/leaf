package com.leaf.rpc.controller;

import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 令牌桶
 */
public class RateLimitFlowController implements FlowController {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitFlowController.class);

    private RateLimiter rateLimiter;

    public RateLimitFlowController(long qps) {
        rateLimiter = RateLimiter.create(qps);
    }

    @Override
    public void flowController() throws RejectedExecutionException {
        if (!rateLimiter.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
            String message = String.format("CounterFlowController rate:[%s]", rateLimiter.getRate());
            logger.debug(message);
            throw new RejectedExecutionException(message);
        }
    }
}
