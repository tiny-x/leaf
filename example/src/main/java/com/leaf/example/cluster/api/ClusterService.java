package com.leaf.example.cluster.api;

import com.leaf.common.annotation.ServiceInterface;

import java.util.HashMap;

@ServiceInterface(group = "cluster-test")
public interface ClusterService {

    String getName();

    void addUser(HashMap<String, Object> user);

    int getAge();
}
