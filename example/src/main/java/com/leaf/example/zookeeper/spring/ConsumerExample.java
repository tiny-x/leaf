package com.leaf.example.zookeeper.spring;

import com.leaf.example.spring.HelloService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ConsumerExample {

    public static void main(String[] args) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:/spring/spring-consumer-zk.xml");
        HelloService service = ctx.getBean(HelloService.class);
        try {
            String sayHello = service.sayHello("   biu biu biu");
            System.out.println(sayHello);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
