package com.example.price_comaprator_backend;

import com.opencsv.bean.CsvToBeanBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BasketOptimizationService {

    private static final Logger logger = LoggerFactory.getLogger(BasketOptimizationService.class);
    private final List<PriceAlert> userAlerts = new ArrayList<>();
    private final List<String> csvFilenames = List.of(
            "altex_2025-05-20.csv",
            "emag_2025-05-20.csv",
            "kaufland_2025-05-01.csv",
            "kaufland_2025-05-08.csv",
            "lidl_2025-05-01.csv",
            "lidl_2025-05-08.csv",
            "profi_2025-05-01.csv",
            "profi_2025-05-08.csv"
    );

    private final List<Product> allProducts;

    public BasketOptimizationService() {
        logger.info("Loading all products from CSV resources...");
        this.allProducts = loadAllProducts();
        logger.info("✅ Loaded {} products total", allProducts.size());
    }

    public List<OptimizedBasketItem> optimizeBasket(ShoppingBasket basket) {
        if (basket == null || basket.getItems() == null || basket.getItems().isEmpty()) {
            logger.warn("Received empty or null basket to optimize");
            return Collections.emptyList();
        }

        if (allProducts == null || allProducts.isEmpty()) {
            logger.error("❗ Product list is empty. Cannot optimize basket.");
            return Collections.emptyList();
        }


        Map<String, Integer> groupedQuantities = basket.getItems().stream()
                .collect(Collectors.groupingBy(
                        item -> normalize(item.getProductName()),
                        Collectors.summingInt(BasketItem::getQuantity)
                ));

        List<OptimizedBasketItem> optimizedItems = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : groupedQuantities.entrySet()) {
            String normalizedInput = entry.getKey();
            int totalQuantity = entry.getValue();
            List<String> inputTokens = tokenize(normalizedInput);

            logger.info("🔍 Searching for: '{}' (normalized)", normalizedInput);

            // 1. Try exact normalized match
            Optional<Product> exactMatch = allProducts.stream()
                    .filter(p -> p.getProductName() != null)
                    .filter(p -> normalize(p.getProductName()).equals(normalizedInput))
                    .min(Comparator.comparing(Product::getPrice));

            if (exactMatch.isPresent()) {
                Product product = exactMatch.get();
                logger.info("✅ Exact match: {} ({}) at {} from {}",
                        product.getProductName(), product.getBrand(), product.getPrice(), product.getSource());

                optimizedItems.add(new OptimizedBasketItem(
                        product.getProductName(),
                        totalQuantity,
                        product.getSource(),
                        product.getPrice()
                ));
                continue;
            }

            // 2. Jaccard token similarity fallback
            List<Map.Entry<Product, Double>> scoredMatches = allProducts.stream()
                    .filter(p -> p.getProductName() != null)
                    .map(p -> {
                        List<String> productTokens = tokenize(normalize(p.getProductName()));
                        double score = tokenOverlapScore(inputTokens, productTokens);
                        return new AbstractMap.SimpleEntry<>(p, score);
                    })
                    .filter(entry2 -> entry2.getValue() >= 0.2)
                    .sorted((e1, e2) -> {
                        int cmp = Double.compare(e2.getValue(), e1.getValue());
                        if (cmp == 0) {
                            return Double.compare(e1.getKey().getPrice(), e2.getKey().getPrice());
                        }
                        return cmp;
                    })
                    .collect(Collectors.toList());

            if (!scoredMatches.isEmpty()) {
                Product product = scoredMatches.get(0).getKey();
                logger.info("✅ Fallback match: {} ({}) at {} from {} [score: {}]",
                        product.getProductName(), product.getBrand(), product.getPrice(), product.getSource(), scoredMatches.get(0).getValue());

                optimizedItems.add(new OptimizedBasketItem(
                        product.getProductName(),
                        totalQuantity,
                        product.getSource(),
                        product.getPrice()
                ));
            } else {
                logger.warn("❌ No match found for '{}'", normalizedInput);


                allProducts.stream()
                        .filter(p -> p.getProductName() != null)
                        .sorted((p1, p2) -> Double.compare(
                                tokenOverlapScore(inputTokens, tokenize(normalize(p2.getProductName()))),
                                tokenOverlapScore(inputTokens, tokenize(normalize(p1.getProductName())))
                        ))
                        .limit(5)
                        .forEach(p -> logger.info("🔍 Candidate: {}", p.getProductName()));
            }
        }

        return optimizedItems;
    }


    public List<OptimizedShoppingList> optimizeAndSplitByStore(ShoppingBasket basket) {
        List<OptimizedBasketItem> flatList = optimizeBasket(basket);
        if (flatList.isEmpty()) {
            logger.warn("No optimized items found to split by store");
            return Collections.emptyList();
        }

        Map<String, List<OptimizedBasketItem>> itemsByStore = flatList.stream()
                .collect(Collectors.groupingBy(OptimizedBasketItem::getStore));

        return itemsByStore.entrySet().stream()
                .map(entry -> new OptimizedShoppingList(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public List<Product> loadAllProducts() {
        List<Product> products = new ArrayList<>();
        ClassLoader classLoader = getClass().getClassLoader();

        for (String filename : csvFilenames) {
            try (InputStream is = classLoader.getResourceAsStream(filename)) {
                if (is == null) {
                    logger.error("Resource not found: {}", filename);
                    continue;
                }

                List<Product> fileProducts = new CsvToBeanBuilder<Product>(new InputStreamReader(is))
                        .withType(Product.class)
                        .withSeparator(';')
                        .build()
                        .parse();

                List<Product> validProducts = fileProducts.stream()
                        .filter(p -> p.getProductName() != null && p.getPrice() != null)
                        .peek(p -> {
                            p.setSource(filename);
                            logger.debug("📦 Loaded product: '{}' normalized as '{}'",
                                    p.getProductName(), normalize(p.getProductName()));
                        })
                        .collect(Collectors.toList());

                products.addAll(validProducts);
                logger.info("✅ Loaded {} valid products from {}", validProducts.size(), filename);
            } catch (Exception e) {
                logger.error("❌ Error reading file {}: {}", filename, e.getMessage());
            }
        }

        return products;
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9 ]", "")
                .trim();
    }

    private static List<String> tokenize(String input) {
        if (input == null || input.isBlank()) return Collections.emptyList();
        return Arrays.asList(input.split("\\s+"));
    }

    private static double tokenOverlapScore(List<String> tokens1, List<String> tokens2) {
        Set<String> set1 = new HashSet<>(tokens1);
        Set<String> set2 = new HashSet<>(tokens2);
        long intersection = set1.stream().filter(set2::contains).count();
        long union = set1.size() + set2.size() - intersection;
        return union > 0 ? (double) intersection / union : 0.0;
    }

    public void addPriceAlert(PriceAlert alert) {
        userAlerts.add(alert);
        logger.info("🔔 Added price alert for '{}' at {} RON", alert.getProductName(), alert.getTargetPrice());
    }

    public List<PriceAlertMatch> checkPriceAlerts() {
        List<PriceAlertMatch> matches = new ArrayList<>();

        for (PriceAlert alert : userAlerts) {
            String normalizedAlertName = normalize(alert.getProductName());

            allProducts.stream()
                    .filter(p -> normalize(p.getProductName()).equals(normalizedAlertName))
                    .filter(p -> p.getPrice() <= alert.getTargetPrice())
                    .forEach(p -> {
                        matches.add(new PriceAlertMatch(
                                p.getProductName(),
                                p.getPrice(),
                                alert.getTargetPrice(),
                                p.getSource()
                        ));
                        logger.info("✅ Alert matched: '{}' at {} in {} (target was {})",
                                p.getProductName(), p.getPrice(), p.getSource(), alert.getTargetPrice());
                    });
        }

        return matches;
    }
}
