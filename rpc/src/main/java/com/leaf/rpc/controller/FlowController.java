package com.leaf.rpc.controller;

import java.util.concurrent.RejectedExecutionException;

public interface FlowController {

    void flowController() throws RejectedExecutionException;
}
