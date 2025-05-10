package com.example.price_comaprator_backend;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/basket")
public class BasketController {

    private final ShoppingBasket basket = new ShoppingBasket();

    @PostMapping("/add")
    public String addItem(@RequestBody BasketItem item) {
        basket.addItem(item);
        return "Item added to basket.";
    }

    @GetMapping
    public ShoppingBasket getBasket() {
        return basket;
    }
}
