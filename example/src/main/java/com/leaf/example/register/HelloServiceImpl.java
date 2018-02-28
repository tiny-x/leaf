package com.leaf.example.register;

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
        logger.info("HelloServiceImpl param:{}", name);
        return "hello" + name;
    }
}
