package com.example.price_comaprator_backend;

import java.util.List;

public class OptimizedShoppingList {
    private String store;
    private List<OptimizedBasketItem> items;
    private double totalCost;

    public OptimizedShoppingList(String store, List<OptimizedBasketItem> items) {
        this.store = store;
        this.items = items;
        this.totalCost = items.stream()
                .mapToDouble(OptimizedBasketItem::getTotalPrice)
                .sum();
    }

    public String getStore() {
        return store;
    }

    public List<OptimizedBasketItem> getItems() {
        return items;
    }

    public double getTotalCost() {
        return totalCost;
    }
}
