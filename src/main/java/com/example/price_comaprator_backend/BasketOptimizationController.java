package com.example.price_comaprator_backend;



import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/optimize")
public class BasketOptimizationController {

    private final BasketOptimizationService optimizationService;

    public BasketOptimizationController(BasketOptimizationService optimizationService) {
        this.optimizationService = optimizationService;
    }

    @PostMapping
    public List<OptimizedBasketItem> optimize(@RequestBody ShoppingBasket basket) {
        return optimizationService.optimizeBasket(basket);
    }
}
