package com.example.price_comaprator_backend;

import com.opencsv.bean.CsvToBeanBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.*;

@Service
public class BasketOptimizationService {

    private static final Logger logger = LoggerFactory.getLogger(BasketOptimizationService.class);

    private final List<String> csvFilenames = List.of(
            "emag_2025-05-20.csv",
            "kaufland_2025-05-01.csv",
            "kaufland_2025-05-08.csv",
            "lidl_2025-05-01.csv",
            "lidl_2025-05-08.csv",
            "profi_2025-05-01.csv",
            "profi_2025-05-08.csv",
            "altex_2025-05-20.csv"
    );

    private List<Product> allProducts;

    public BasketOptimizationService() {
        logger.info("Loading all products from CSV resources...");
        this.allProducts = loadAllProducts();
        logger.info("Loaded {} products", allProducts.size());
    }

    public List<OptimizedBasketItem> optimizeBasket(ShoppingBasket basket) {
        if (basket == null || basket.getItems() == null || basket.getItems().isEmpty()) {
            logger.warn("Received empty or null basket to optimize");
            return Collections.emptyList();
        }

        List<OptimizedBasketItem> optimizedList = new ArrayList<>();

        for (BasketItem item : basket.getItems()) {
            String normalizedItemName = normalize(item.getProductName());
            logger.info("Optimizing for basket item: {}", item.getProductName());

            Optional<Product> cheapestMatch = allProducts.stream()
                    .filter(p -> normalize(p.getProductName()).contains(normalizedItemName))
                    .min(Comparator.comparing(Product::getPrice));

            if (cheapestMatch.isPresent()) {
                Product product = cheapestMatch.get();
                logger.info("Match found: {} at price {} from {}", product.getProductName(), product.getPrice(), product.getSource());
                optimizedList.add(new OptimizedBasketItem(
                        product.getProductName(),
                        item.getQuantity(),
                        product.getSource(),
                        product.getPrice()
                ));
            } else {
                logger.warn("No product match found for basket item: {}", item.getProductName());
            }
        }

        return optimizedList;
    }

    public List<OptimizedShoppingList> optimizeAndSplitByStore(ShoppingBasket basket) {
        List<OptimizedBasketItem> optimizedItems = optimizeBasket(basket);
        if (optimizedItems.isEmpty()) {
            logger.warn("No optimized items found to split by store");
            return Collections.emptyList();
        }

        Map<String, List<OptimizedBasketItem>> itemsByStore = new HashMap<>();
        for (OptimizedBasketItem item : optimizedItems) {
            itemsByStore.computeIfAbsent(item.getStore(), k -> new ArrayList<>()).add(item);
        }

        List<OptimizedShoppingList> shoppingLists = new ArrayList<>();
        for (Map.Entry<String, List<OptimizedBasketItem>> entry : itemsByStore.entrySet()) {
            shoppingLists.add(new OptimizedShoppingList(entry.getKey(), entry.getValue()));
        }

        return shoppingLists;
    }

    private List<Product> loadAllProducts() {
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
                        .build()
                        .parse();

                fileProducts.forEach(p -> p.setSource(filename));
                products.addAll(fileProducts);
                logger.info("Loaded {} products from {}", fileProducts.size(), filename);

            } catch (Exception e) {
                logger.error("Failed to load products from resource: {}", filename, e);
            }
        }

        return products;
    }

    private static String normalize(String s) {
        if (s == null) return "";
        // Normalize Unicode characters and remove diacritics (accents)
        String normalized = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "") // Remove diacritical marks
                .toLowerCase();
        // Remove all non-alphanumeric characters (keep only letters and digits)
        normalized = normalized.replaceAll("[^a-z0-9]", "");
        return normalized;
    }
}
