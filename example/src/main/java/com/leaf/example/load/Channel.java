package com.leaf.example.load;

public class Channel {

    private String name;

    private int weight;

    public Channel(String name, int weight) {
        this.name = name;
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }
}
