package com.example.price_comaprator_backend;


import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/basket")
public class BasketController {

    private final ShoppingBasket basket = new ShoppingBasket();
    private final BasketOptimizationService optimizer;

    public BasketController(BasketOptimizationService optimizer) {
        this.optimizer = optimizer;
    }

    @PostMapping("/add")
    public String addItem(@RequestBody BasketItem item) {
        basket.addItem(item);
        return "Item added to basket.";
    }

    @GetMapping
    public ShoppingBasket getBasket() {
        return basket;
    }

    @GetMapping("/optimize")
    public List<OptimizedBasketItem> optimizeBasket() {
        return optimizer.optimizeBasket(basket);
    }
}


