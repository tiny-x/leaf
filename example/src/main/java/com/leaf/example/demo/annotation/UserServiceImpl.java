package com.leaf.example.demo.annotation;

import com.leaf.common.annotation.ServiceProvider;

import java.util.Arrays;
import java.util.List;

@ServiceProvider
public class UserServiceImpl implements UserService {

    @Override
    public List<User> getAllUser() {
        User[] users = new User[]{
                new User(1, "小米", 10),
                new User(1, "小百", 12),
                new User(1, "小黑", 14),
        };
        return Arrays.asList(users);
    }

    @Override
    public void addUser(User user) {

    }

    @Override
    public void addMultiUser(List<User> user) {

    }
}
