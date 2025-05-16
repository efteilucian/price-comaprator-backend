package com.example.price_comaprator_backend;

import com.opencsv.bean.CsvToBeanBuilder;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class DiscountService {

    public List<Discount> loadDiscounts(List<String> fileNames) {
        List<Discount> discounts = new ArrayList<>();

        for (String fileName : fileNames) {
            try {
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("discounts/" + fileName);
                if (inputStream == null) {
                    System.err.println("❌ Could not load " + fileName);
                    continue;
                }

                Reader reader = new InputStreamReader(inputStream);
                List<Discount> fileDiscounts = new CsvToBeanBuilder<Discount>(reader)
                        .withType(Discount.class)
                        .withIgnoreLeadingWhiteSpace(true)
                        .withSeparator(';')
                        .build()
                        .parse();

                // 🏷️ Extract store name (e.g., "kaufland" from "kaufland_discounts_2025-05-08.csv")
                String store = fileName.split("_")[0];
                for (Discount discount : fileDiscounts) {
                    discount.setSource(store);
                }

                discounts.addAll(fileDiscounts);
            } catch (Exception e) {
                System.err.println("❌ Failed to load " + fileName + ": " + e.getMessage());
            }
        }

        return discounts;
    }


    public List<Discount> findDiscountsForBasket(List<BasketItem> basket, List<Discount> allDiscounts) {
        List<Discount> matched = new ArrayList<>();

        for (BasketItem item : basket) {
            String basketProductName = item.getProductName().trim().toLowerCase();
            String basketBrand = item.getBrand().trim().toLowerCase();

            for (Discount discount : allDiscounts) {
                if (discount.getProductName() != null && discount.getBrand() != null) {
                    String discountProductName = discount.getProductName().trim().toLowerCase();
                    String discountBrand = discount.getBrand().trim().toLowerCase();

                    if (basketProductName.equals(discountProductName) && basketBrand.equals(discountBrand)) {
                        matched.add(discount);
                    }
                }
            }
        }

        return matched;
    }
}
