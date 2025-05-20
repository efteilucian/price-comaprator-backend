package com.example.price_comaprator_backend;

import java.time.LocalDate;

public class PriceHistoryEntry {
    private String productName;
    private String brand;
    private String store;
    private String category;
    private LocalDate date;
    private double price;

    public PriceHistoryEntry(String productName, String brand, String store, String category, LocalDate date, double price) {
        this.productName = productName;
        this.brand = brand;
        this.store = store;
        this.category = category;
        this.date = date;
        this.price = price;
    }



    public String getProductName() { return productName; }
    public String getBrand() { return brand; }
    public String getStore() { return store; }
    public String getCategory() { return category; }
    public LocalDate getDate() { return date; }
    public double getPrice() { return price; }

    public void setProductName(String productName) { this.productName = productName; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setStore(String store) { this.store = store; }
    public void setCategory(String category) { this.category = category; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setPrice(double price) { this.price = price; }
}
