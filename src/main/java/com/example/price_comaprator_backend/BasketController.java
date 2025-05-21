package com.example.price_comaprator_backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/basket")
public class BasketController {

    private static final Logger logger = LoggerFactory.getLogger(BasketController.class);


    private final Basket sessionBasket = new Basket();
    private final DiscountService discountService;
    private final BasketOptimizationService optimizationService;


    private final List<Product> controllerLoadedProducts;
    private final List<Discount> controllerLoadedDiscounts;

    public BasketController(DiscountService discountService, CSVLoaderService csvLoaderService,
                            BasketOptimizationService optimizationService) {
        this.discountService = discountService;

        this.optimizationService = optimizationService;

        logger.info("BasketController initializing...");
        logger.warn("BasketController loads its own product and discount data. This data is NOT synchronized with BasketOptimizationService's reload mechanism or DiscountService's potential updates.");

        // Loads an initial snapshot of products specific to this controller instance.
        this.controllerLoadedProducts = csvLoaderService.loadAllCSVs(List.of(
                "emag_2025-05-20.csv",
                "kaufland_2025-05-01.csv", "kaufland_2025-05-08.csv",
                "lidl_2025-05-01.csv", "lidl_2025-05-08.csv",
                "profi_2025-05-01.csv", "profi_2025-05-08.csv",
                "altex_2025-05-20.csv"
        ));
        logger.info("BasketController: Loaded {} products for its internal use.", this.controllerLoadedProducts.size());


        this.controllerLoadedDiscounts = discountService.loadDiscounts(List.of(
                "altex_discounts-2025-05-20.csv", "emag_discounts_2025-05-20.csv",
                "kaufland_discounts_2025-05-01.csv", "kaufland_discounts_2025-05-08.csv",
                "lidl_discounts_2025-05-01.csv", "lidl_discounts_2025-05-08.csv",
                "profi_discounts_2025-05-01.csv", "profi_discounts_2025-05-08.csv"
        ));
        logger.info("BasketController: Loaded {} discounts for its internal use.", this.controllerLoadedDiscounts.size());
    }

    @PostMapping("/add")
    public ResponseEntity<String> addToBasket(@RequestBody BasketItem item) {
        logger.info("Request to add to basket: Product='{}', Brand='{}', Quantity={}, Source='{}'",
                item.getProductName(), item.getBrand(), item.getQuantity(), item.getSource());

        // Validates if the product (name, brand, source combination) exists in the controller's loaded product list.
        boolean productExists = controllerLoadedProducts.stream().anyMatch(p ->
                p.getProductName().equalsIgnoreCase(item.getProductName()) &&
                        (item.getBrand() == null || p.getBrand() == null || p.getBrand().equalsIgnoreCase(item.getBrand())) &&
                        (item.getSource() == null || p.getSource() == null || p.getSource().equalsIgnoreCase(item.getSource()))
        );

        if (!productExists) {
            logger.warn("Attempt to add non-existent product to basket: Name='{}', Brand='{}', Source='{}'",
                    item.getProductName(), item.getBrand(), item.getSource());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("❌ Product not found in known products. Please check name, brand, and source.");
        }

        sessionBasket.getItems().add(item);
        logger.info("✅ Added to basket: {} (Brand: {}, Qty: {})", item.getProductName(), item.getBrand(), item.getQuantity());
        return ResponseEntity.ok("✅ Added to basket: " + item.getProductName() + " (" + item.getBrand() + ")");
    }

    @GetMapping
    public List<BasketItem> getBasket() {
        logger.info("Request to get basket contents. Current item count: {}", sessionBasket.getItems().size());
        return new ArrayList<>(sessionBasket.getItems());
    }

    @DeleteMapping("/remove")
    public ResponseEntity<String> removeFromBasket(@RequestBody BasketItem itemToRemove) {
        logger.info("Request to remove from basket: Product='{}', Brand='{}', Source='{}'",
                itemToRemove.getProductName(), itemToRemove.getBrand(), itemToRemove.getSource());

        boolean removed = sessionBasket.getItems().removeIf(basketItem ->
                basketItem.getProductName().equalsIgnoreCase(itemToRemove.getProductName()) &&
                        (itemToRemove.getBrand() == null || basketItem.getBrand() == null || basketItem.getBrand().equalsIgnoreCase(itemToRemove.getBrand())) &&
                        (itemToRemove.getSource() == null || basketItem.getSource() == null || basketItem.getSource().equalsIgnoreCase(itemToRemove.getSource()))
        );

        if (removed) {
            logger.info("✅ Removed from basket: {} ({})", itemToRemove.getProductName(), itemToRemove.getBrand());
            return ResponseEntity.ok("✅ Removed from basket: " + itemToRemove.getProductName() + " (" + itemToRemove.getBrand() + ")");
        } else {
            logger.warn("Item not found in basket for removal: Name='{}', Brand='{}', Source='{}'",
                    itemToRemove.getProductName(), itemToRemove.getBrand(), itemToRemove.getSource());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("❌ Item not found in basket.");
        }
    }

    @GetMapping("/discounts")
    public List<Discount> getDiscountsForBasket() {
        logger.info("Request for discounts applicable to current basket.");
        // Uses the controller's initially loaded snapshot of discounts.
        List<Discount> relevantDiscounts = discountService.findDiscountsForBasket(sessionBasket.getItems(), controllerLoadedDiscounts);
        logger.info("Found {} relevant discounts for the basket.", relevantDiscounts.size());
        return relevantDiscounts;
    }

    @GetMapping("/discounts/new")
    public List<Discount> getNewDiscounts(@RequestParam(defaultValue = "1") int days) {
        logger.info("Request for new discounts from the last {} day(s).", days);

        List<Discount> newDiscounts = discountService.findNewDiscounts(controllerLoadedDiscounts, days);
        logger.info("Found {} new discounts.", newDiscounts.size());
        return newDiscounts;
    }

    @GetMapping("/optimize")
    public List<OptimizedShoppingList> optimizeCurrentBasket() {
        logger.info("Request to optimize current session basket.");
        ShoppingBasket currentShoppingBasket = getBasketAsShoppingBasket();
        if (currentShoppingBasket.getItems().isEmpty()) {
            logger.info("Basket is empty, no optimization to perform.");
            return Collections.emptyList();
        }

        List<OptimizedShoppingList> optimizedResult = optimizationService.optimizeAndSplitByStore(currentShoppingBasket);
        logger.info("Basket optimization complete. Result contains {} store-specific lists.", optimizedResult.size());
        return optimizedResult;
    }


    private ShoppingBasket getBasketAsShoppingBasket() {
        return new ShoppingBasket(new ArrayList<>(sessionBasket.getItems()));
    }

    @PostMapping("/clear")
    public ResponseEntity<String> clearBasket() {
        logger.info("Request to clear basket. Current items: {}", sessionBasket.getItems().size());
        sessionBasket.getItems().clear();
        logger.info("Basket cleared successfully.");
        return ResponseEntity.ok("Basket cleared");
    }
}