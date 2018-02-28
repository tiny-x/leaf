package com.leaf.register.api.model;

public enum DirectoryEnums {

    PROVIDERS("providers"),

    CONSUMERS("consumers");

    public String path;

    DirectoryEnums(String path) {
        this.path = path;
    }
}
