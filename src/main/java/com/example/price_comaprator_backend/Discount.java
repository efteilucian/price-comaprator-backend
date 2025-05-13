package com.example.price_comaprator_backend;

import java.time.LocalDate;

public class Discount {
    private String productId;
    private String productName;
    private String brand;
    private double packageQuantity;
    private String packageUnit;
    private String productCategory;
    private LocalDate fromDate;
    private LocalDate toDate;
    private int percentageOfDiscount;
    private String source; // e.g., "kaufland", "lidl", "profi"

    public Discount() {}

    public Discount(String productId, String productName, String brand, double packageQuantity,
                    String packageUnit, String productCategory, LocalDate fromDate, LocalDate toDate,
                    int percentageOfDiscount, String source) {
        this.productId = productId;
        this.productName = productName;
        this.brand = brand;
        this.packageQuantity = packageQuantity;
        this.packageUnit = packageUnit;
        this.productCategory = productCategory;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.percentageOfDiscount = percentageOfDiscount;
        this.source = source;
    }



    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public double getPackageQuantity() {
        return packageQuantity;
    }

    public void setPackageQuantity(double packageQuantity) {
        this.packageQuantity = packageQuantity;
    }

    public String getPackageUnit() {
        return packageUnit;
    }

    public void setPackageUnit(String packageUnit) {
        this.packageUnit = packageUnit;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public int getPercentageOfDiscount() {
        return percentageOfDiscount;
    }

    public void setPercentageOfDiscount(int percentageOfDiscount) {
        this.percentageOfDiscount = percentageOfDiscount;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "Discount{" +
                "productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", brand='" + brand + '\'' +
                ", packageQuantity=" + packageQuantity +
                ", packageUnit='" + packageUnit + '\'' +
                ", productCategory='" + productCategory + '\'' +
                ", fromDate=" + fromDate +
                ", toDate=" + toDate +
                ", percentageOfDiscount=" + percentageOfDiscount +
                ", source='" + source + '\'' +
                '}';
    }
}
