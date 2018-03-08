package com.leaf.example.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloServiceImpl implements HelloService {

    private final static Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);

    @Override
    public String sayHello(String name) {
        logger.info("HelloServiceImpl param:{}", name);
        return "hello" + name;
    }
}
