package com.example.price_comaprator_backend;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PriceAlertService {

    private final BasketOptimizationService basketOptimizationService;
    private final List<PriceAlert> alerts = new ArrayList<>();

    public PriceAlertService(BasketOptimizationService basketOptimizationService) {
        this.basketOptimizationService = basketOptimizationService;
    }

    public void addAlert(PriceAlert alert) {
        alerts.add(alert);
    }

    public List<PriceAlert> getAllAlerts() {
        return new ArrayList<>(alerts);
    }

    public List<PriceAlert> checkAlerts() {
        List<Product> allProducts = basketOptimizationService.loadAllProducts();
        List<PriceAlert> triggeredAlerts = new ArrayList<>();

        for (PriceAlert alert : alerts) {
            String normalizedAlertName = normalize(alert.getProductName());

            List<Product> matchingProducts = allProducts.stream()
                    .filter(p -> p.getProductName() != null)
                    .filter(p -> normalize(p.getProductName()).equals(normalizedAlertName))
                    .filter(p -> p.getPrice() <= alert.getTargetPrice())
                    .collect(Collectors.toList());

            for (Product product : matchingProducts) {
                triggeredAlerts.add(new PriceAlert(
                        product.getProductName(),
                        alert.getTargetPrice(),
                        product.getPrice(),
                        product.getSource()
                ));
            }
        }

        return triggeredAlerts;
    }



    private String normalize(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9 ]", "")
                .trim();
    }
}
