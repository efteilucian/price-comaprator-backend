package com.example.price_comaprator_backend;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/basket")
public class BasketController {

    private final Basket basket = new Basket();
    private final DiscountService discountService;
    private final CSVLoaderService csvLoaderService;
    private final BasketOptimizationService optimizationService;

    private final List<Product> allProducts;
    private final List<Discount> allDiscounts;

    public BasketController(DiscountService discountService, CSVLoaderService csvLoaderService,
                            BasketOptimizationService optimizationService) {
        this.discountService = discountService;
        this.csvLoaderService = csvLoaderService;
        this.optimizationService = optimizationService;

        // Load products (for validation)
        this.allProducts = csvLoaderService.loadAllCSVs(List.of(
                "emag_2025-05-20.csv",
                "kaufland_2025-05-01.csv",
                "kaufland_2025-05-08.csv",
                "lidl_2025-05-01.csv",
                "lidl_2025-05-08.csv",
                "profi_2025-05-01.csv",
                "profi_2025-05-08.csv",
                "altex_2025-05-20.csv"
        ));

        // Load discounts
        this.allDiscounts = discountService.loadDiscounts(List.of(
                "altex_discounts-2025-05-20.csv",
                "emag_discounts_2025-05-20.csv",
                "kaufland_discounts_2025-05-01.csv",
                "kaufland_discounts_2025-05-08.csv",
                "lidl_discounts_2025-05-01.csv",
                "lidl_discounts_2025-05-08.csv",
                "profi_discounts_2025-05-01.csv",
                "profi_discounts_2025-05-08.csv"
        ));
    }

    @PostMapping("/add")
    public ResponseEntity<String> addToBasket(@RequestBody BasketItem item) {
        boolean productExists = allProducts.stream().anyMatch(p ->
                p.getProductName().equalsIgnoreCase(item.getProductName()) &&
                        p.getBrand().equalsIgnoreCase(item.getBrand()) &&
                        p.getSource().equalsIgnoreCase(item.getSource())
        );

        if (!productExists) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("❌ Product not found in known products. Please check name, brand, and source.");
        }

        basket.getItems().add(item);
        return ResponseEntity.ok("✅ Added to basket: " + item.getProductName() + " (" + item.getBrand() + ")");
    }

    @GetMapping
    public List<BasketItem> getBasket() {
        return new ArrayList<>(basket.getItems());
    }

    // Remove item from basket by matching all key fields
    @DeleteMapping("/remove")
    public ResponseEntity<String> removeFromBasket(@RequestBody BasketItem item) {
        boolean removed = basket.getItems().removeIf(basketItem ->
                basketItem.getProductName().equalsIgnoreCase(item.getProductName()) &&
                        basketItem.getBrand().equalsIgnoreCase(item.getBrand()) &&
                        basketItem.getSource().equalsIgnoreCase(item.getSource())
        );

        if (removed) {
            return ResponseEntity.ok("✅ Removed from basket: " + item.getProductName() + " (" + item.getBrand() + ")");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("❌ Item not found in basket.");
        }
    }

    @GetMapping("/discounts")
    public List<Discount> getDiscountsForBasket() {
        return discountService.findDiscountsForBasket(basket.getItems(), allDiscounts);
    }

    @GetMapping("/discounts/new")
    public List<Discount> getNewDiscounts(@RequestParam(defaultValue = "1") int days) {
        return discountService.findNewDiscounts(allDiscounts, days);
    }

    // NEW: Optimize the current basket
    @GetMapping("/optimize")
    public List<OptimizedBasketItem> optimizeBasket() {
        return optimizationService.optimizeBasket(getBasketAsShoppingBasket());
    }

    // Helper to convert Basket to ShoppingBasket
    private ShoppingBasket getBasketAsShoppingBasket() {
        return new ShoppingBasket(new ArrayList<>(basket.getItems()));
    }

    @PostMapping("/clear")
    public ResponseEntity<String> clearBasket() {
        basket.getItems().clear();
        return ResponseEntity.ok("Basket cleared");
    }

}
