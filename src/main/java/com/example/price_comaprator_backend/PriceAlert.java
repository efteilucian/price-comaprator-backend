package com.example.price_comaprator_backend;


public class PriceAlert {
    private String productName;
    private double targetPrice;


    private Double currentPrice;
    private String store;

    public PriceAlert() {}

    public PriceAlert(String productName, double targetPrice) {
        this.productName = productName;
        this.targetPrice = targetPrice;
    }


    public PriceAlert(String productName, double targetPrice, Double currentPrice, String store) {
        this.productName = productName;
        this.targetPrice = targetPrice;
        this.currentPrice = currentPrice;
        this.store = store;
    }


    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public double getTargetPrice() { return targetPrice; }
    public void setTargetPrice(double targetPrice) { this.targetPrice = targetPrice; }

    public Double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(Double currentPrice) { this.currentPrice = currentPrice; }

    public String getStore() { return store; }
    public void setStore(String store) { this.store = store; }
}

