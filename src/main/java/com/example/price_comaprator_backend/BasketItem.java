package com.example.price_comaprator_backend;

public class BasketItem {
    private String productName;
    private int quantity;


    public BasketItem() {}
    public BasketItem(String productName, int quantity) {
        this.productName = productName;
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
}

