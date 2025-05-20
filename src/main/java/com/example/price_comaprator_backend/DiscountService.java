package com.example.price_comaprator_backend;

import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
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
            String basketSource = item.getSource().trim().toLowerCase();  // Normalize here

            for (Discount discount : allDiscounts) {
                if (discount.getProductName() != null && discount.getBrand() != null && discount.getSource() != null) {
                    String discountProductName = discount.getProductName().trim().toLowerCase();
                    String discountBrand = discount.getBrand().trim().toLowerCase();
                    String discountSource = discount.getSource().trim().toLowerCase();

                    if (basketProductName.equals(discountProductName)
                            && basketBrand.equals(discountBrand)
                            && basketSource.equals(discountSource)) {
                        matched.add(discount);
                    }
                }
            }
        }

        return matched;
    }


    public List<Discount> getBestDiscounts(List<Discount> allDiscounts, int limit) {
        Map<String, Discount> bestPerProduct = new HashMap<>();

        for (Discount d : allDiscounts) {
            String key = (d.getProductName() + "_" + d.getBrand()).toLowerCase().trim();


            if (!bestPerProduct.containsKey(key) || d.getPercentageOfDiscount() > bestPerProduct.get(key).getPercentageOfDiscount()) {
                bestPerProduct.put(key, d);
            }
        }

        return bestPerProduct.values().stream()
                .sorted(Comparator.comparing(Discount::getPercentageOfDiscount).reversed())
                .limit(limit)
                .toList();
    }

    public List<Discount> findNewDiscounts(List<Discount> allDiscounts, int days) {
        LocalDate cutoffDate = LocalDate.now().minusDays(days);

        return allDiscounts.stream()
                .filter(d -> d.getFromDate() != null && !d.getFromDate().isBefore(cutoffDate))
                .collect(Collectors.toList());
    }



}
