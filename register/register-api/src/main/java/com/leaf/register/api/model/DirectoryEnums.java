package com.leaf.register.api.model;

public enum DirectoryEnums {

    PROVIDERS("providers"),

    CONSUMERS("consumersServiceMeta");

    public String path;

    DirectoryEnums(String path) {
        this.path = path;
    }
}
