package com.example.price_comaprator_backend;

import java.util.ArrayList;
import java.util.List;

public class ShoppingBasket {
    private List<BasketItem> items = new ArrayList<>();

    public List<BasketItem> getItems() {
        return items;
    }

    public void addItem(BasketItem item) {
        this.items.add(item);
    }
}
