package com.example.price_comaprator_backend;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);
    private final BasketOptimizationService optimizationService;

    public RecommendationService(BasketOptimizationService optimizationService) {
        this.optimizationService = optimizationService;
    }

    public List<Product> getBetterValueAlternatives(String productNameQuery) {
        List<Product> allProducts = optimizationService.getAllProducts();
        if (allProducts == null || allProducts.isEmpty()) {
            logger.warn("Product list is empty. Cannot provide recommendations for '{}'", productNameQuery);
            return Collections.emptyList();
        }

        String normalizedQuery = normalize(productNameQuery);
        logger.info("Recommendation query: '{}' (normalized: '{}')", productNameQuery, normalizedQuery);


        // sorted to prioritize name matches first, then by the best value (lowest price per standard unit).
        List<Product> potentialReferences = allProducts.stream()
                .filter(p -> p.getProductName() != null && p.getPricePerStandardUnit() != null &&
                        p.getBaseUnitType() != UnitConverter.BaseUnitType.UNKNOWN &&
                        p.getCurrency() != null)
                .filter(p -> normalize(p.getProductName()).contains(normalizedQuery))
                .sorted(
                        Comparator.<Product, Integer>comparing(p -> normalize(p.getProductName()).equals(normalizedQuery) ? 0 : 1)
                                .thenComparing(Product::getPricePerStandardUnit, Comparator.nullsLast(Double::compareTo))
                )
                .collect(Collectors.toList());

        if (potentialReferences.isEmpty()) {
            logger.warn("No suitable reference product found for query: '{}'", productNameQuery);
            return Collections.emptyList();
        }


        Product referenceProduct = potentialReferences.get(0);
        UnitConverter.BaseUnitType refBaseUnitType = referenceProduct.getBaseUnitType();

        Double refPricePerStandardUnitNullable = referenceProduct.getPricePerStandardUnit();
        if (refPricePerStandardUnitNullable == null) {

            logger.warn("Reference product '{}' has null pricePerStandardUnit. Cannot proceed with recommendations, returning only reference.", referenceProduct.getProductName());
            return Collections.singletonList(referenceProduct);
        }
        double refPricePerStandardUnit = refPricePerStandardUnitNullable;

        String refCurrency = referenceProduct.getCurrency();
        String refCategory = referenceProduct.getProductCategory();
        String refProductId = referenceProduct.getProductId();

        logger.info("Primary Reference Product: '{}' (ID: {}), Category: {}, Price/StdUnit: {} {}/{}, Type: {}",
                referenceProduct.getProductName(), refProductId, refCategory,
                String.format("%.2f", refPricePerStandardUnit), refCurrency, referenceProduct.getStandardUnit(), refBaseUnitType);
        List<Product> betterValueAlternatives = allProducts.stream()
                .filter(p -> !Objects.equals(p.getProductId(), refProductId))
                .filter(p -> p.getPricePerStandardUnit() != null && p.getBaseUnitType() != UnitConverter.BaseUnitType.UNKNOWN)
                .filter(p -> p.getBaseUnitType() == refBaseUnitType)
                .filter(p -> Objects.equals(p.getCurrency(), refCurrency))
                .filter(p -> Objects.equals(p.getProductCategory(), refCategory))
                .filter(p -> isPotentiallyRelated(normalize(p.getProductName()), normalizedQuery, refBaseUnitType))
                .filter(p -> p.getPricePerStandardUnit() < refPricePerStandardUnit)
                .sorted(
                        Comparator.comparing(Product::getPricePerStandardUnit, Comparator.nullsLast(Double::compareTo))
                                .thenComparing(Product::getProductName, Comparator.nullsLast(String::compareToIgnoreCase))
                )
                .limit(5)
                .peek(p -> logger.info("Better Value Suggestion: '{}' (ID: {}), Price/StdUnit: {} {}/{}",
                        p.getProductName(), p.getProductId(), String.format("%.2f", p.getPricePerStandardUnit()), p.getCurrency(), p.getStandardUnit()))
                .collect(Collectors.toList());


        if (betterValueAlternatives.isEmpty()) {
            logger.info("No 'better value per unit' alternatives found. Looking for cheapest overall variants of related products or the reference itself.");


            List<Product> sameProductVariants = potentialReferences.stream()
                    .filter(p -> Objects.equals(p.getCurrency(), refCurrency))
                    .filter(p -> Objects.equals(p.getProductCategory(), refCategory))
                    .filter(p -> p.getBaseUnitType() == refBaseUnitType)
                    .filter(p -> p.getPricePerStandardUnit() != null)
                    .sorted(
                            Comparator.comparing(Product::getPricePerStandardUnit, Comparator.nullsLast(Double::compareTo))
                                    .thenComparing(Product::getPrice, Comparator.nullsLast(Double::compareTo))
                                    .thenComparing(Product::getProductName, Comparator.nullsLast(String::compareToIgnoreCase))
                    )
                    .limit(5)
                    .peek(p -> logger.info("Fallback Suggestion (Same Product Variant/Cheapest): '{}' (ID: {}), Price/StdUnit: {} {}/{}",
                            p.getProductName(), p.getProductId(), String.format("%.2f", p.getPricePerStandardUnit()), p.getCurrency(), p.getStandardUnit()))
                    .collect(Collectors.toList());

            if (!sameProductVariants.isEmpty()) {
                return sameProductVariants;
            } else {

                if (referenceProduct.getPricePerStandardUnit() != null) {
                    logger.info("Fallback: Returning original reference product as no other variants or alternatives found.");
                    return Collections.singletonList(referenceProduct);
                } else {

                    logger.warn("Fallback: Original reference product ('{}') itself is not comparable (e.g., null price/std unit after all). No suggestions.", referenceProduct.getProductName());
                    return Collections.emptyList();
                }
            }
        }
        return betterValueAlternatives;
    }

    /**
     * Helper method to determine if a product name is considered "related enough" to the user's query
     * to be suggested as an alternative. The strictness can vary by product type.
     *
     * @param productNameNormalized The normalized name of the potential alternative product.
     * @param queryNormalized       The normalized user query.
     * @param type                  The BaseUnitType of the reference product, used to apply different heuristics.
     * @return true if the product is considered related, false otherwise.
     */
    private boolean isPotentiallyRelated(String productNameNormalized, String queryNormalized, UnitConverter.BaseUnitType type) {
        if (productNameNormalized == null || queryNormalized == null) return false;


        if (type == UnitConverter.BaseUnitType.COUNT) {
            if (queryNormalized.split("\\s+").length <= 2) {
                return productNameNormalized.contains(queryNormalized) || queryNormalized.contains(productNameNormalized);
            }
            return productNameNormalized.contains(queryNormalized);
        }


        return productNameNormalized.contains(queryNormalized) || sharesKeyTokens(productNameNormalized, queryNormalized);
    }

    /**
     *  check if two strings share any common significant tokens (words).
     */
    private boolean sharesKeyTokens(String s1, String s2) {
        if (s1 == null || s2 == null) return false;
        String[] tokens1 = s1.split("\\s+");
        String[] tokens2 = s2.split("\\s+");
        for (String t1 : tokens1) {
            if (t1.length() < 3) continue;
            for (String t2 : tokens2) {
                if (t2.length() < 3) continue;
                if (t1.equals(t2)) return true;
            }
        }
        return false;
    }

    /**
     * Normalizes a string for consistent searching and comparison.
     * Steps:
     * 1. Decomposes accented characters to base character + combining diacritical mark.
     * 2. Removes all diacritical marks.
     * 3. Converts to lowercase.
     * 4. Removes characters that are not lowercase letters, digits, or whitespace.
     * 5. Trims leading/trailing whitespace.
     * 6. Replaces multiple consecutive whitespace characters with a single space.
     */
    private static String normalize(String s) {
        if (s == null) return "";
        String normalized = Normalizer.normalize(s, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", "");
        normalized = normalized.toLowerCase();
        normalized = normalized.replaceAll("[^a-z0-9\\s]", "");
        normalized = normalized.trim();
        normalized = normalized.replaceAll("\\s+", " ");
        return normalized;
    }
}