package com.leaf.example.register;

import com.leaf.common.annotation.ServiceInterface;

@ServiceInterface(group = "register-demo")
public interface HelloService {

    String sayHello(String name);
}
