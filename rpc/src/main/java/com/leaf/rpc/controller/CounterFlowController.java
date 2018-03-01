package com.leaf.rpc.controller;

import java.util.LinkedList;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 计数器
 */
public class CounterFlowController implements FlowController {

    private final static LinkedList<Long> RANGE_LIST = new LinkedList<>();
    private final AtomicBoolean isFlow = new AtomicBoolean(false);
    private final AtomicLong atomicLong = new AtomicLong(0);
    private final AtomicBoolean isRun = new AtomicBoolean(false);
    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private final long thresholdQps;

    private volatile long currentQps;

    public CounterFlowController(long thresholdQps) {
        this.thresholdQps = thresholdQps;
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
    }

    @Override
    public void flowController() throws RejectedExecutionException {
        atomicLong.incrementAndGet();
        if (isRun.compareAndSet(false, true)) {
            scheduledThreadPoolExecutor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    RANGE_LIST.addLast(atomicLong.get());
                    if (RANGE_LIST.size() > 10) {
                        RANGE_LIST.removeFirst();
                    }
                    if ((currentQps = RANGE_LIST.peekLast() - RANGE_LIST.peekFirst()) > thresholdQps) {
                        isFlow.compareAndSet(false, true);
                    } else {
                        isFlow.compareAndSet(true, false);
                    }
                }
            }, 10, 100, TimeUnit.MILLISECONDS);
        }

        if (isFlow.get()) {
            String message = String.format("CounterFlowController qps more than:[%d], current: [%d]",
                    thresholdQps, currentQps);
            throw new RejectedExecutionException(message);
        }
    }

}