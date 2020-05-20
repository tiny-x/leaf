package com.leaf.example.demo;

import com.leaf.common.context.RpcContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yefei
 * @date 2017-06-20 14:14
 */
public class HelloServiceImpl implements HelloService {

    /**
     * logger
     */
    private final static Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);

    @Override
    public String sayHello(String name) {
        return "hello" + name;
    }
}
