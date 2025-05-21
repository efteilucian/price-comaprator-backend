package com.example.price_comaprator_backend;

public class PriceAlert {
    private String productName;
    private double targetPrice;
    private String currency; // for the target price

    // These fields are for when an alert is triggered

    private Double currentPriceOnMatch; // The price of the product that met the alert criteria
    private String storeOnMatch;        // The store where the matching product was found

    public PriceAlert() {}

    //  user CREATES an alert
    // The frontend/client should provide these three pieces of information.
    public PriceAlert(String productName, double targetPrice, String currency) {
        this.productName = productName;
        this.targetPrice = targetPrice;
        this.currency = currency;
    }

    // Constructor for when the SYSTEM IDENTIFIES a triggered alert
    // This is useful for returning information about the match.
    public PriceAlert(String productName, double targetPrice, String currency, Double currentPriceOnMatch, String storeOnMatch) {
        this.productName = productName;             // Name of the product that matched
        this.targetPrice = targetPrice;             // Original target price of the alert
        this.currency = currency;                   // Original currency of the alert
        this.currentPriceOnMatch = currentPriceOnMatch; // Actual price of the matched product
        this.storeOnMatch = storeOnMatch;               // Store where the product was found
    }

    // Getters and Setters
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public double getTargetPrice() { return targetPrice; }
    public void setTargetPrice(double targetPrice) { this.targetPrice = targetPrice; }

    public String getCurrency() { return currency; } // <<--- ADDED GETTER
    public void setCurrency(String currency) { this.currency = currency; } // <<--- ADDED SETTER

    public Double getCurrentPriceOnMatch() { return currentPriceOnMatch; }
    public void setCurrentPriceOnMatch(Double currentPriceOnMatch) { this.currentPriceOnMatch = currentPriceOnMatch; }

    public String getStoreOnMatch() { return storeOnMatch; }
    public void setStoreOnMatch(String storeOnMatch) { this.storeOnMatch = storeOnMatch; }
}