package com.leaf.example.spring;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ProviderExample {

    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("classpath:/spring/spring-provider.xml");
    }
}
