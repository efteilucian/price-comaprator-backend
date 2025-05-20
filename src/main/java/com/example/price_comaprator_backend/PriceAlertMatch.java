package com.example.price_comaprator_backend;

public class PriceAlertMatch {

    private String productName;
    private double currentPrice;
    private double targetPrice;
    private String store;

    public PriceAlertMatch() {}

    public PriceAlertMatch(String productName, double currentPrice, double targetPrice, String store) {
        this.productName = productName;
        this.currentPrice = currentPrice;
        this.targetPrice = targetPrice;
        this.store = store;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public double getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(double targetPrice) {
        this.targetPrice = targetPrice;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }
}
