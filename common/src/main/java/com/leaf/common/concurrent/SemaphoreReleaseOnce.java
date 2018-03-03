package com.leaf.common.concurrent;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class SemaphoreReleaseOnce {

    private Semaphore semaphore;

    private AtomicBoolean releaseOnce = new AtomicBoolean(false);

    public SemaphoreReleaseOnce(Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    public void release() {
        if (releaseOnce.compareAndSet(false, true)) {
            semaphore.release();
        }
    }
}
