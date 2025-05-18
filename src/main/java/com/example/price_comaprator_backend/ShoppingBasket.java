package com.example.price_comaprator_backend;

import java.util.ArrayList;
import java.util.List;

public class ShoppingBasket {
    private List<BasketItem> items = new ArrayList<>();

    // Constructor accepting a list of BasketItem
    public ShoppingBasket(List<BasketItem> items) {
        this.items = items;
    }

    // Default constructor (optional, but recommended)
    public ShoppingBasket() {
        this.items = new ArrayList<>();
    }

    public List<BasketItem> getItems() {
        return items;
    }

    public void addItem(BasketItem item) {
        this.items.add(item);
    }
}
