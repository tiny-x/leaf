package com.leaf.example.cluster.api;

import com.leaf.common.annotation.ServiceProvider;

import java.util.HashMap;

@ServiceProvider
public class SuccessServiceImpl implements ClusterService {

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void addUser(HashMap<String, Object> user) {

    }

    @Override
    public int getAge() {
        return 0;
    }
}
