package com.example.clot.models;

public class Sizes {
    private String id;
    private String name;

    // Конструктор, геттеры и сеттеры
    public Sizes(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }
}