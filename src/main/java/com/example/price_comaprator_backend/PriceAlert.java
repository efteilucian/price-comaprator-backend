package com.example.price_comaprator_backend;

public class PriceAlert {
    private String productName;
    private double targetPrice;
    private String currency;



    private Double currentPriceOnMatch;
    private String storeOnMatch;

    public PriceAlert() {}


    public PriceAlert(String productName, double targetPrice, String currency) {
        this.productName = productName;
        this.targetPrice = targetPrice;
        this.currency = currency;
    }


    public PriceAlert(String productName, double targetPrice, String currency, Double currentPriceOnMatch, String storeOnMatch) {
        this.productName = productName;
        this.targetPrice = targetPrice;
        this.currency = currency;
        this.currentPriceOnMatch = currentPriceOnMatch;
        this.storeOnMatch = storeOnMatch;
    }


    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public double getTargetPrice() { return targetPrice; }
    public void setTargetPrice(double targetPrice) { this.targetPrice = targetPrice; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Double getCurrentPriceOnMatch() { return currentPriceOnMatch; }
    public void setCurrentPriceOnMatch(Double currentPriceOnMatch) { this.currentPriceOnMatch = currentPriceOnMatch; }

    public String getStoreOnMatch() { return storeOnMatch; }
    public void setStoreOnMatch(String storeOnMatch) { this.storeOnMatch = storeOnMatch; }
}