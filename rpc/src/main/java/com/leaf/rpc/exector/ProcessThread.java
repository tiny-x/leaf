package com.leaf.rpc.exector;

/**
 * 业务线程
 *
 * @author yefei
 */
public class ProcessThread extends Thread {

    public ProcessThread(Runnable target) {
        super(target);
    }
}
