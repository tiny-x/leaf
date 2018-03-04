package com.leaf.example.demo.annotation;

import com.leaf.common.annotation.ServiceInterface;

import java.util.List;

@ServiceInterface(group = "core")
public interface UserService {

    List<User> getAllUser();

    void addUser(User user);

    void addMultiUser(List<User> user);
}
