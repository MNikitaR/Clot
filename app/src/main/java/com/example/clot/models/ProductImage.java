package com.example.clot.models;

public class ProductImage {
    public int id;
    public int product_id;
    public String image_url;

    public ProductImage(int id, int product_id, String image_url) {
        this.id = id;
        this.product_id = product_id;
        this.image_url = image_url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }
}
