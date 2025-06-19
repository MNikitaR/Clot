package com.example.clot.models;

public class Address {
    private String id;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private boolean isDefault;

    // Конструкторы
    public Address() {}

    public Address(String id, String street, String city, String state, String zipCode, boolean isDefault) {
        this.id = id;
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.isDefault = isDefault;
    }

    // Геттеры
    public String getId() { return id; }
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getZipCode() { return zipCode; }
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    // Форматированный адрес
    public String getFormattedAddress() {
        return street + "\n" + city + ", " + state + " " + zipCode;
    }
}