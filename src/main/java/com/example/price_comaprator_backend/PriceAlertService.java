package com.example.price_comaprator_backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PriceAlertService {

    private static final Logger logger = LoggerFactory.getLogger(PriceAlertService.class);
    private final BasketOptimizationService basketOptimizationService;
    private final List<PriceAlert> alerts = new ArrayList<>(); // User-defined alerts managed by this service

    public PriceAlertService(BasketOptimizationService basketOptimizationService) {
        this.basketOptimizationService = basketOptimizationService;
    }

    public void addAlert(PriceAlert alert) {
        if (alert.getProductName() == null || alert.getProductName().isBlank() ||
                alert.getCurrency() == null || alert.getCurrency().isBlank() ||
                alert.getTargetPrice() <= 0) { // Added target price validation
            logger.warn("Attempted to add an invalid alert (null/blank fields or non-positive target price): {}", alert);

            return;
        }
        alerts.add(alert);
        logger.info("🔔 Alert added via PriceAlertService: '{}' for {} {}",
                alert.getProductName(), alert.getTargetPrice(), alert.getCurrency());
    }

    public List<PriceAlert> getAllAlerts() {
        return new ArrayList<>(alerts); // Return a copy
    }

    public List<PriceAlert> checkAlerts() {
        List<Product> allProducts = basketOptimizationService.getAllProducts(); // Gets the current (potentially reloaded) list
        List<PriceAlert> triggeredAlerts = new ArrayList<>();

        if (allProducts == null || allProducts.isEmpty()) {
            logger.warn("PriceAlertService.checkAlerts() - Product list is empty from BasketOptimizationService. Cannot check alerts.");
            return triggeredAlerts;
        }
        logger.info("PriceAlertService.checkAlerts() - Product count from BasketOptimizationService: {}", allProducts.size());

        for (PriceAlert userAlert : alerts) {
            if (userAlert.getCurrency() == null || userAlert.getCurrency().isBlank()) {
                logger.warn("PriceAlertService.checkAlerts() - Skipping alert with no currency: {}", userAlert.getProductName());
                continue;
            }
            String normalizedAlertName = normalize(userAlert.getProductName());

            logger.info("PriceAlertService.checkAlerts() --- Checking Alert ---");
            logger.info("PriceAlertService.checkAlerts() - Alert Details: Name='{}', Normalized='{}', TargetPrice={}, Currency='{}'",
                    userAlert.getProductName(), normalizedAlertName, userAlert.getTargetPrice(), userAlert.getCurrency());

            allProducts.stream()
                    .filter(product -> {
                        if (product.getProductName() == null || product.getPrice() == null || product.getCurrency() == null) {
                            logger.trace("PriceAlertService.checkAlerts() - Product ID '{}' (Name: '{}') missing critical fields (name/price/currency). Skipping.", product.getProductId(), product.getProductName());
                            return false;
                        }
                        return true;
                    })
                    .filter(product -> {
                        String normalizedProductNameFromCSV = normalize(product.getProductName());
                        boolean nameMatch = normalizedProductNameFromCSV.equals(normalizedAlertName);
                        if (!nameMatch) {
                            logger.trace("PriceAlertService.checkAlerts() - Name Mismatch for alert '{}': Product Name='{}' (Normalized='{}') vs Alert Normalized='{}'",
                                    userAlert.getProductName(), product.getProductName(), normalizedProductNameFromCSV, normalizedAlertName);
                        } else {
                            logger.debug("PriceAlertService.checkAlerts() - Name MATCH for alert '{}': Product Name='{}' (Normalized='{}')",
                                    userAlert.getProductName(), product.getProductName(), normalizedProductNameFromCSV);
                        }
                        return nameMatch;
                    })
                    .filter(product -> {
                        boolean currencyMatch = product.getCurrency().equalsIgnoreCase(userAlert.getCurrency());
                        if (!currencyMatch) {
                            logger.trace("PriceAlertService.checkAlerts() - Currency Mismatch for '{}': Product Currency='{}' vs Alert Currency='{}'",
                                    product.getProductName(), product.getCurrency(), userAlert.getCurrency());
                        } else {
                            logger.debug("PriceAlertService.checkAlerts() - Currency MATCH for '{}': Product Currency='{}'", product.getProductName(), product.getCurrency());
                        }
                        return currencyMatch;
                    })
                    .filter(product -> {
                        boolean priceMatch = product.getPrice() <= userAlert.getTargetPrice();
                        if (!priceMatch) {
                            logger.trace("PriceAlertService.checkAlerts() - Price Mismatch for '{}': Product Price={} vs Alert TargetPrice={}",
                                    product.getProductName(), product.getPrice(), userAlert.getTargetPrice());
                        } else {
                            logger.debug("PriceAlertService.checkAlerts() - Price MATCH for '{}': Product Price={} <= Alert TargetPrice={}",
                                    product.getProductName(), product.getPrice(), userAlert.getTargetPrice());
                        }
                        return priceMatch;
                    })
                    .forEach(matchingProduct -> {
                        logger.info("PriceAlertService.checkAlerts() --- INSIDE FOREACH for product: {} ---", matchingProduct.getProductName());
                        PriceAlert triggered = new PriceAlert(
                                matchingProduct.getProductName(),
                                userAlert.getTargetPrice(),
                                userAlert.getCurrency(),
                                matchingProduct.getPrice(),
                                matchingProduct.getSource()
                        );
                        triggeredAlerts.add(triggered);
                        logger.info("PriceAlertService.checkAlerts() - ✅ Alert triggered (PAS): '{}' (target: {} {}) found at {} {} in store {}",
                                matchingProduct.getProductName(), userAlert.getTargetPrice(), userAlert.getCurrency(),
                                matchingProduct.getPrice(), matchingProduct.getCurrency(), matchingProduct.getSource());
                    });
            logger.info("PriceAlertService.checkAlerts() --- Finished Checking Alert for '{}' ---", userAlert.getProductName());
        }
        if (triggeredAlerts.isEmpty() && !alerts.isEmpty()) {
            logger.info("PriceAlertService.checkAlerts() - No alerts were triggered in this check run.");
        }
        return triggeredAlerts;
    }


    private String normalize(String s) {
        if (s == null) return "";
        String normalized = Normalizer.normalize(s, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", "");          // Remove diacritical marks
        normalized = normalized.toLowerCase();                               // To lower case
        normalized = normalized.replaceAll("[^a-z0-9 ]", "");   // Remove non-alphanumeric BUT KEEP SPACES
        normalized = normalized.trim();                                      // Trim leading/trailing

        return normalized;
    }
}