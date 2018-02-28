package com.leaf.example.failover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

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
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "hello" + name;
    }
}
