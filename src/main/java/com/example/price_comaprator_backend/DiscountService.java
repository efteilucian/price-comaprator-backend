package com.example.price_comaprator_backend;

import com.opencsv.bean.CsvToBeanBuilder;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
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
                        .withSeparator(';')  // ✅ Tell OpenCSV to use semicolon
                        .build()
                        .parse();

               // System.out.println("✅ Loaded " + fileDiscounts.size() + " discounts from " + fileName);
               // fileDiscounts.forEach(d -> System.out.println("   - " + d));   debug each line

                discounts.addAll(fileDiscounts);
            } catch (Exception e) {
                System.err.println("❌ Failed to load " + fileName + ": " + e.getMessage());
            }
        }

        return discounts;
    }


    public List<Discount> findDiscountsForBasket(List<BasketItem> basket, List<Discount> allDiscounts) {
        LocalDate today = LocalDate.now();
        List<Discount> matched = new ArrayList<>();

        for (BasketItem item : basket) {
            String basketProductName = item.getProductName().trim().toLowerCase();

            for (Discount discount : allDiscounts) {
                if (discount.getProductName() != null) {
                    String discountProductName = discount.getProductName().trim().toLowerCase();

                    if (basketProductName.equals(discountProductName) &&
                            (discount.getFromDate() == null || !today.isBefore(discount.getFromDate())) &&
                            (discount.getToDate() == null || !today.isAfter(discount.getToDate()))) {

                        matched.add(discount);
                    }
                }
            }
        }

        return matched;
    }
}
