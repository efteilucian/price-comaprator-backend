package com.example.price_comaprator_backend;

import com.opencsv.bean.CsvBindByName;

public class Product {

    @CsvBindByName(column = "product_id")
    private String productId;

    @CsvBindByName(column = "product_name")
    private String productName;

    @CsvBindByName(column = "product_category")
    private String productCategory;

    @CsvBindByName(column = "brand")
    private String brand;

    @CsvBindByName(column = "package_quantity")
    private String packageQuantity;

    @CsvBindByName(column = "package_unit")
    private String packageUnit;

    @CsvBindByName(column = "price")
    private Double price;

    @CsvBindByName(column = "currency")
    private String currency;

    private String source;

    // Getters and setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductCategory() { return productCategory; }
    public void setProductCategory(String productCategory) { this.productCategory = productCategory; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getPackageQuantity() { return packageQuantity; }
    public void setPackageQuantity(String packageQuantity) { this.packageQuantity = packageQuantity; }

    public String getPackageUnit() { return packageUnit; }
    public void setPackageUnit(String packageUnit) { this.packageUnit = packageUnit; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    // New helper to parse quantity as double
    public Double getMeasurementQuantity() {
        if (packageQuantity == null) return null;
        try {
            return Double.parseDouble(packageQuantity);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // New helper to expose unit
    public String getMeasurementUnit() {
        return packageUnit;
    }

    // Compute price per unit (e.g., price per kg, liter, etc.)
    public Double getPricePerUnit() {
        Double quantity = getMeasurementQuantity();
        if (quantity == null || price == null || quantity == 0.0) return null;
        return price / quantity;
    }
}
