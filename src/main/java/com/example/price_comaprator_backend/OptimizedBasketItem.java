package com.example.price_comaprator_backend;


public class OptimizedBasketItem {
    private String productName;
    private int quantity;
    private String store;
    private double unitPrice;
    private double totalPrice;

    public OptimizedBasketItem(String productName, int quantity, String store, double unitPrice) {
        this.productName = productName;
        this.quantity = quantity;
        this.store = store;
        this.unitPrice = unitPrice;
        this.totalPrice = unitPrice * quantity;
    }


    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public String getStore() { return store; }
    public double getUnitPrice() { return unitPrice; }
    public double getTotalPrice() { return totalPrice; }
}
