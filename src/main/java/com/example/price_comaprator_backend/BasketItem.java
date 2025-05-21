package com.example.price_comaprator_backend;

public class BasketItem {
    private String productName;
    private String brand;
    private int quantity;
    private String source;

    public BasketItem() {}

    public BasketItem(String productName, String brand, int quantity, String source) {
        this.productName = productName;
        this.brand = brand;
        this.quantity = quantity;
        this.source = source;
    }

    public String getProductName() {
        return productName;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public String getBrand() {
        return brand;
    }
    public void setBrand(String brand) {
        this.brand = brand;
    }
    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }
}
