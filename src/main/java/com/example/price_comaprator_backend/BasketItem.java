package com.example.price_comaprator_backend;

public class BasketItem {
    private String productName;
    private String brand;
    private int quantity;


    public BasketItem() {}
    public BasketItem(String productName,String brand, int quantity) {
        this.productName = productName;
        this.brand = brand;
        this.quantity = quantity;
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

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
}

