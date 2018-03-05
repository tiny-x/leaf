package com.leaf.example.register;

import com.leaf.common.annotation.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceProvider
public class HelloServiceImpl implements HelloService {

    private final static Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);

    @Override
    public String sayHello(String name) {
        logger.info("HelloServiceImpl param:{}", name);
        return "hello" + name;
    }
}
