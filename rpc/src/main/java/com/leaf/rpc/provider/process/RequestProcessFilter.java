package com.leaf.rpc.provider.process;

import com.leaf.rpc.local.ServiceWrapper;

/**
 * @author yefei
 */
public interface RequestProcessFilter {

    /**
     *
     * @param requestWrapper
     */
    void filter(RequestWrapper requestWrapper, ServiceWrapper serviceWrapper);

    /**
     *
     * @param responseWrapper
     */
    void filter(ResponseWrapper responseWrapper);

}
