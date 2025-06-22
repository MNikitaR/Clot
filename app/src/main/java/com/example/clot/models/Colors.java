package com.example.clot.models;

public class Colors {
    public int id;
    public String name;
    public String hex_code;

    public Colors(int id, String name, String hex_code) {
        this.id = id;
        this.name = name;
        this.hex_code = hex_code;
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

    public String getHex_code() {
        return hex_code;
    }

    public void setHex_code(String hex_code) {
        this.hex_code = hex_code;
    }
}
