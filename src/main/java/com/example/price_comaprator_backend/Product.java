package com.example.price_comaprator_backend;

import com.example.price_comaprator_backend.UnitConverter; // Import the new utility
import com.opencsv.bean.CsvBindByName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Product {
    private static final Logger logger = LoggerFactory.getLogger(Product.class);

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

    // New fields for standardized comparison
    private Double pricePerStandardUnit;
    private String standardUnit; // e.g., "kg", "l", "item"
    private UnitConverter.BaseUnitType baseUnitType;

    // Getters and setters for existing fields...
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

    // --- Enhanced Methods ---

    // Call this after setting packageQuantity, packageUnit, and price (e.g., after loading from CSV)
    public void calculateStandardizedMetrics() {
        if (this.productName == null) { // Basic sanity check
            logger.warn("Calculating standardized metrics for product with null name. This might indicate an issue.");
        }
        if (this.packageUnit == null || this.packageUnit.trim().isEmpty() ||
                this.packageQuantity == null || this.packageQuantity.trim().isEmpty() ||
                this.price == null) {
            this.pricePerStandardUnit = null;
            this.standardUnit = (this.packageUnit != null && !this.packageUnit.trim().isEmpty()) ? this.packageUnit.toLowerCase().trim() : "unknown";
            this.baseUnitType = UnitConverter.getBaseUnitType(this.packageUnit); // Still try to get type if unit exists
            if(this.price == null) logger.trace("Product '{}': Price is null, cannot calculate price per unit.", this.productName);
            else if (this.packageQuantity == null || this.packageQuantity.trim().isEmpty()) logger.trace("Product '{}': PackageQuantity is null/empty.", this.productName);
            else if (this.packageUnit == null || this.packageUnit.trim().isEmpty()) logger.trace("Product '{}': PackageUnit is null/empty.", this.productName);
            return;
        }

        Double quantityValue = getMeasurementQuantity(); // Parses packageQuantity to Double
        if (quantityValue == null || quantityValue == 0.0) {
            this.pricePerStandardUnit = null;
            this.standardUnit = this.packageUnit.toLowerCase().trim();
            this.baseUnitType = UnitConverter.getBaseUnitType(this.packageUnit);
            logger.trace("Product '{}': quantityValue is null or zero after parsing '{}'. Price/StdUnit set to null.", this.productName, this.packageQuantity);
            return;
        }

        // <<< MODIFICATION FOR ALWAYS COUNT CATEGORIES >>>
        if (UnitConverter.isAlwaysCountCategory(this.productCategory)) {
            this.baseUnitType = UnitConverter.BaseUnitType.COUNT;
            this.standardUnit = "item";
            // For these categories, we consider the product as a single countable unit,
            // regardless of its listed package_quantity or package_unit if they imply weight/volume.
            // The price is for this one "item".
            this.pricePerStandardUnit = this.price / 1.0; // Price for 1 item.
            logger.trace("Product '{}' in category '{}' treated as COUNT, price/item: {}",
                    this.productName, this.productCategory, this.pricePerStandardUnit);
            return; // Skip further unit conversion for these categories
        }
        // <<< END OF ALWAYS COUNT MODIFICATION >>>

        String originalUnitClean = this.packageUnit.toLowerCase().trim();
        this.baseUnitType = UnitConverter.getBaseUnitType(originalUnitClean);
        this.standardUnit = UnitConverter.getBaseUnit(originalUnitClean); // e.g., "g" -> "kg", or "box" -> "box" if not in map
        Double conversionFactor = UnitConverter.getConversionFactor(originalUnitClean); // e.g., "g" -> 0.001

        if (conversionFactor != null && this.baseUnitType != UnitConverter.BaseUnitType.UNKNOWN) {
            double quantityInStandardUnits = quantityValue * conversionFactor;
            if (quantityInStandardUnits > 0) {
                this.pricePerStandardUnit = this.price / quantityInStandardUnits;
            } else {
                this.pricePerStandardUnit = null;
                logger.warn("Product '{}': Calculated quantity in standard units is not positive ({}, from original qty {} {} with factor {}). Price/StdUnit set to null.",
                        this.productName, quantityInStandardUnits, quantityValue, originalUnitClean, conversionFactor);
            }
        } else {
            // Unit not recognized by UnitConverter or type is UNKNOWN for a non-ALWAYS_COUNT category
            this.pricePerStandardUnit = null;
            // standardUnit would have been set to originalUnitClean by getBaseUnit's default
            // baseUnitType would have been set to UNKNOWN by getBaseUnitType's default
            logger.trace("Product '{}': No conversion factor or unknown unit type for unit '{}'. Price/StdUnit set to null.", this.productName, originalUnitClean);
        }
    }

    // Helper to parse packageQuantity (String) into Double
    public Double getMeasurementQuantity() {
        if (packageQuantity == null || packageQuantity.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(packageQuantity.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            logger.error("Could not parse quantity string: '{}' for product '{}'. Error: {}", packageQuantity, productName, e.getMessage());
            return null;
        }
    }

    public String getOriginalPackageUnit() {
        return packageUnit;
    }

    public Double getPricePerOriginalUnit() {
        Double quantity = getMeasurementQuantity();
        if (quantity == null || price == null || quantity == 0.0) return null;
        return price / quantity;
    }

    public Double getPricePerStandardUnit() {
        return pricePerStandardUnit;
    }

    public String getStandardUnit() {
        return standardUnit;
    }

    public UnitConverter.BaseUnitType getBaseUnitType() {
        return baseUnitType;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productName='" + productName + '\'' +
                ", packageQuantity='" + packageQuantity + '\'' +
                ", packageUnit='" + packageUnit + '\'' +
                ", price=" + price +
                ", currency='" + currency + '\'' +
                ", productCategory='" + productCategory + '\'' + // Added category
                ", pricePerStandardUnit=" + pricePerStandardUnit +
                ", standardUnit='" + standardUnit + '\'' +
                ", baseUnitType=" + baseUnitType +
                ", source='" + source + '\'' +
                '}';
    }
}