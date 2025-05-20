package com.example.price_comaprator_backend;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
@Service
public class RecommendationService {
    private final BasketOptimizationService optimizationService;

    public RecommendationService(BasketOptimizationService optimizationService) {
        this.optimizationService = optimizationService;
    }

    public List<Product> getBetterValueAlternatives(String productName) {
        List<Product> allProducts = optimizationService.loadAllProducts();
        String normalizedName = normalize(productName);
        List<String> inputTokens = tokenize(normalizedName);

        // Find reference product(s)
        List<Product> reference = allProducts.stream()
                .filter(p -> normalize(p.getProductName()).contains(normalizedName))
                .collect(Collectors.toList());

        if (reference.isEmpty()) return List.of();

        Product ref = reference.get(0);  // First match
        String unit = ref.getMeasurementUnit();
        String category = ref.getProductCategory();

        double refPricePerUnit = ref.getPricePerUnit() != null ? ref.getPricePerUnit() : Double.MAX_VALUE;

        return allProducts.stream()
                .filter(p -> p.getPricePerUnit() != null)
                .filter(p -> Objects.equals(p.getMeasurementUnit(), unit))
                .filter(p -> Objects.equals(p.getProductCategory(), category))
                .filter(p -> !normalize(p.getProductName()).equals(normalizedName))
                .filter(p -> p.getPricePerUnit() < refPricePerUnit)
                .sorted(Comparator.comparing(Product::getPricePerUnit))
                .limit(5)
                .collect(Collectors.toList());
    }

    private static String normalize(String s) {
        return s == null ? "" : s.toLowerCase().replaceAll("[^a-z0-9 ]", "").trim();
    }

    private static List<String> tokenize(String input) {
        return Arrays.asList(normalize(input).split("\\s+"));
    }
}
