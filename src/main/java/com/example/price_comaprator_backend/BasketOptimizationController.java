package com.example.price_comaprator_backend;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/optimize")
public class BasketOptimizationController {

    private final BasketOptimizationService optimizationService;

    public BasketOptimizationController(BasketOptimizationService optimizationService) {
        this.optimizationService = optimizationService;
    }

    @PostMapping("/flat")
    public ResponseEntity<List<OptimizedBasketItem>> optimizeFlat(@RequestBody ShoppingBasket basket) {
        if (basket == null || basket.getItems().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<OptimizedBasketItem> result = optimizationService.optimizeBasket(basket);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/by-store")
    public ResponseEntity<List<OptimizedShoppingList>> optimizeByStore(@RequestBody ShoppingBasket basket) {
        if (basket == null || basket.getItems().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<OptimizedShoppingList> result = optimizationService.optimizeAndSplitByStore(basket);
        return ResponseEntity.ok(result);
    }
}
