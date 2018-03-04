package com.leaf.example.cluster.api;

import java.util.HashMap;

public class FailServiceImpl implements ClusterService {

    @Override
    public String getName() {
        throw new RuntimeException("no user name!");
    }

    @Override
    public void addUser(HashMap<String, Object> user) {
        throw new RuntimeException("add user fail!");
    }

    @Override
    public int getAge() {
        throw new RuntimeException("no user age!");
    }
}
