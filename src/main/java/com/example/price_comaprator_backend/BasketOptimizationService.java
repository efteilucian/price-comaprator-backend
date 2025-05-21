package com.example.price_comaprator_backend;

import com.opencsv.bean.CsvToBeanBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BasketOptimizationService {

    private static final Logger logger = LoggerFactory.getLogger(BasketOptimizationService.class);


    private final List<PriceAlert> userAlerts_BOS = new ArrayList<>();

    private final List<String> csvFilenames = List.of(
            "altex_2025-05-20.csv", "emag_2025-05-20.csv",
            "kaufland_2025-05-01.csv", "kaufland_2025-05-08.csv",
            "lidl_2025-05-01.csv", "lidl_2025-05-08.csv",
            "profi_2025-05-01.csv", "profi_2025-05-08.csv"
    );

    private List<Product> allProducts = new ArrayList<>();

    public BasketOptimizationService() {
        logger.info("BasketOptimizationService: Initializing and loading products...");
        this.allProducts = loadAllProductsFromCsv();
        logger.info("BasketOptimizationService: ✅ Initially loaded and processed {} products total.", this.allProducts.size());
    }

    public synchronized List<Product> getAllProducts() {
        return this.allProducts;
    }

    private List<Product> loadAllProductsFromCsv() {
        logger.info("BasketOptimizationService: Starting to load product data from all CSVs.");
        List<Product> products = new ArrayList<>();
        ClassLoader classLoader = getClass().getClassLoader();

        for (String filename : csvFilenames) {
            logger.debug("BasketOptimizationService: Attempting to load file: {}", filename);
            try (InputStream is = classLoader.getResourceAsStream(filename);
                 InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(is, "InputStream for " + filename + " was null. File not found in resources?"), StandardCharsets.UTF_8)) {

                List<Product> fileProducts = new CsvToBeanBuilder<Product>(reader)
                        .withType(Product.class)
                        .withSeparator(';')
                        .withIgnoreLeadingWhiteSpace(true)
                        .build()
                        .parse();

                List<Product> validProducts = fileProducts.stream()
                        .filter(p -> p.getProductName() != null && !p.getProductName().trim().isEmpty() &&
                                p.getPrice() != null && p.getPrice() > 0 &&
                                p.getPackageUnit() != null && !p.getPackageUnit().trim().isEmpty() &&
                                p.getPackageQuantity() != null && !p.getPackageQuantity().trim().isEmpty())
                        .peek(p -> {
                            p.setSource(filename.split("_")[0]);
                            logger.trace("RAW_LOAD - File: {}, Product: '{}', Parsed Price: {}, Parsed Currency: {}",
                                    filename, p.getProductName(), p.getPrice(), p.getCurrency());
                            p.calculateStandardizedMetrics();
                            logger.trace("METRICS_CALC - Product: '{}', Category: {}, StdUnit: {}, Price/StdUnit: {}, Src: {}",
                                    p.getProductName(), p.getProductCategory(), p.getStandardUnit(),
                                    (p.getPricePerStandardUnit() != null ? String.format("%.2f", p.getPricePerStandardUnit()) : "null"),
                                    p.getSource());
                        })
                        .collect(Collectors.toList());

                products.addAll(validProducts);
                logger.info("BasketOptimizationService: Loaded {} valid products from {}.", validProducts.size(), filename);
            } catch (NullPointerException e) {
                logger.error("BasketOptimizationService: ❌ Resource not found (NullPointerException) for: {}. Check filename. Error: {}", filename, e.getMessage(), e);
            } catch (Exception e) {
                logger.error("BasketOptimizationService: ❌ Error reading or processing file {}: {}", filename, e.getMessage(), e);
            }
        }
        logger.info("BasketOptimizationService: Finished loading products from CSVs. Total processed: {}.", products.size());
        return products;
    }

    public synchronized void refreshProducts() {
        logger.info("BasketOptimizationService: <<<< Starting manual refresh of products... >>>>");
        this.allProducts = loadAllProductsFromCsv();
        logger.info("BasketOptimizationService: ✅<<<< Finished manual refresh. {} products loaded. >>>>", this.allProducts.size());


    }

    public List<OptimizedBasketItem> optimizeBasket(ShoppingBasket basket) {
        logger.info("Optimizing basket with {} item types.", basket.getItems() != null ? basket.getItems().size() : 0);
        if (basket == null || basket.getItems() == null || basket.getItems().isEmpty()) {
            logger.warn("Received empty or null basket to optimize, returning empty list.");
            return Collections.emptyList();
        }

        List<Product> currentProductList = getAllProducts();
        if (currentProductList.isEmpty()) {
            logger.error("Product list is empty. Cannot optimize basket.");
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
            logger.debug("Optimizing item: '{}' (normalized), quantity: {}", normalizedInput, totalQuantity);


            Optional<Product> bestPriceMatch = currentProductList.stream()
                    .filter(p -> p.getProductName() != null && p.getPrice() != null)
                    .filter(p -> normalize(p.getProductName()).equals(normalizedInput))
                    .min(Comparator.comparing(Product::getPrice));

            if (bestPriceMatch.isPresent()) {
                Product product = bestPriceMatch.get();
                logger.info("Optimize - Exact name match for '{}': Found {} from {} at price {}.",
                        normalizedInput, product.getProductName(), product.getSource(), product.getPrice());
                optimizedItems.add(new OptimizedBasketItem(product.getProductName(), totalQuantity, product.getSource(), product.getPrice()));
                continue;
            }


            List<String> inputTokens = tokenize(normalizedInput);
            List<Map.Entry<Product, Double>> scoredMatches = currentProductList.stream()
                    .filter(p -> p.getProductName() != null && p.getPrice() != null)
                    .map(p -> new AbstractMap.SimpleEntry<>(p, tokenOverlapScore(inputTokens, tokenize(normalize(p.getProductName())))))
                    .filter(e -> e.getValue() >= 0.2)
                    .sorted((e1, e2) -> {
                        int cmp = Double.compare(e2.getValue(), e1.getValue());
                        return (cmp == 0) ? Double.compare(e1.getKey().getPrice(), e2.getKey().getPrice()) : cmp;
                    })
                    .collect(Collectors.toList());

            if (!scoredMatches.isEmpty()) {
                Product product = scoredMatches.get(0).getKey();
                logger.info("Optimize - Fallback similarity match for '{}': Found {} from {} at price {} (Score: {:.2f})",
                        normalizedInput, product.getProductName(), product.getSource(), product.getPrice(), scoredMatches.get(0).getValue());
                optimizedItems.add(new OptimizedBasketItem(product.getProductName(), totalQuantity, product.getSource(), product.getPrice()));
            } else {
                logger.warn("Optimize - No match (exact or similarity) found for basket item: '{}'", normalizedInput);
            }
        }
        logger.info("Basket optimization finished. Found {} optimized items.", optimizedItems.size());
        return optimizedItems;
    }

    public List<OptimizedShoppingList> optimizeAndSplitByStore(ShoppingBasket basket) {
        logger.info("Splitting optimized basket by store.");
        List<OptimizedBasketItem> flatList = optimizeBasket(basket);
        if (flatList.isEmpty()) {
            logger.warn("No optimized items to split by store.");
            return Collections.emptyList();
        }
        // Groups the optimized items by store to create per-store shopping lists.
        Map<String, List<OptimizedBasketItem>> itemsByStore = flatList.stream()
                .collect(Collectors.groupingBy(OptimizedBasketItem::getStore));

        List<OptimizedShoppingList> result = itemsByStore.entrySet().stream()
                .map(e -> new OptimizedShoppingList(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        logger.info("Optimized basket split into {} store lists.", result.size());
        return result;
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
        if (tokens1.isEmpty() || tokens2.isEmpty()) return 0.0;
        Set<String> set1 = new HashSet<>(tokens1);
        Set<String> set2 = new HashSet<>(tokens2);
        set1.remove(""); set2.remove("");
        if (set1.isEmpty() || set2.isEmpty()) return 0.0;

        long intersectionSize = set1.stream().filter(set2::contains).count();
        long unionSize = set1.size() + set2.size() - intersectionSize;
        return unionSize > 0 ? (double) intersectionSize / unionSize : 0.0;
    }



    public void addPriceAlert_BOS(PriceAlert alert) {
        userAlerts_BOS.add(alert);
        logger.info("(BOS Internal) Added price alert for: '{}', Target: {} {}",
                alert.getProductName(), alert.getTargetPrice(), alert.getCurrency());
    }

    public List<PriceAlertMatch> checkPriceAlerts_BOS() {
        logger.info("(BOS Internal) Checking {} internal alerts.", userAlerts_BOS.size());
        List<PriceAlertMatch> matches = new ArrayList<>();
        List<Product> currentProductList = getAllProducts();

        if (currentProductList.isEmpty()) {
            logger.warn("(BOS Internal) Product list is empty. Cannot check internal alerts.");
            return matches;
        }
        for (PriceAlert alert : userAlerts_BOS) {
            if (alert.getCurrency() == null || alert.getCurrency().isBlank()) {
                logger.warn("(BOS Internal) Skipping internal alert for '{}' due to missing currency.", alert.getProductName());
                continue;
            }
            String normalizedAlertName = normalize(alert.getProductName());
            currentProductList.stream()
                    .filter(p -> p.getProductName() != null && p.getPrice() != null && p.getCurrency() != null)
                    .filter(p -> normalize(p.getProductName()).equals(normalizedAlertName))
                    .filter(p -> p.getCurrency().equalsIgnoreCase(alert.getCurrency()))
                    .filter(p -> p.getPrice() <= alert.getTargetPrice())
                    .forEach(p -> {
                        matches.add(new PriceAlertMatch(
                                p.getProductName(), p.getPrice(), alert.getTargetPrice(), p.getSource()));
                        logger.info("✅ (BOS Internal) Alert matched: '{}' at {} {} in store {} (Target: {} {})",
                                p.getProductName(), p.getPrice(), p.getCurrency(), p.getSource(),
                                alert.getTargetPrice(), alert.getCurrency());
                    });
        }
        logger.info("(BOS Internal) Finished checking internal alerts. {} matches found.", matches.size());
        return matches;
    }
}