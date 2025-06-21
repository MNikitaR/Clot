package com.example.clot.models;

public class Category{
    public int id;
    public String name;
    public String images_url;

    public Category(int id, String name, String images_url) {
        this.id = id;
        this.name = name;
        this.images_url = images_url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImages_url() {
        return images_url;
    }

    public void setImages_url(String images_url) {
        this.images_url = images_url;
    }
}