package com.leaf.example.demo;

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

    @Override
    public String sayHello(String name, String age) {
        return "hello:" + name + ",age:" + age;
    }

    @Override
    public String sayHello(User user) {
        logger.info("HelloServiceImpl param:{}", user.getName());
        return "HelloServiceImpl param: " + user.getName();
    }
}
