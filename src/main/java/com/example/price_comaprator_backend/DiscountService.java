package com.example.price_comaprator_backend;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DiscountService {

    private final String discountsDirectory = "src/main/resources/discounts";

    public List<Discount> getActiveDiscountsForBasket(ShoppingBasket basket) {
        List<Discount> allDiscounts = loadAllDiscounts();
        LocalDate today = LocalDate.now();

        Set<String> basketProductNames = basket.getItems().stream()
                .map(BasketItem::getProductName)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return allDiscounts.stream()
                .filter(discount -> basketProductNames.contains(discount.getProductName().toLowerCase()))
                .filter(discount -> !today.isBefore(discount.getFromDate()) && !today.isAfter(discount.getToDate()))
                .collect(Collectors.toList());
    }

    private List<Discount> loadAllDiscounts() {
        List<Discount> discounts = new ArrayList<>();
        File folder = new File(discountsDirectory);

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".csv"));
        if (files == null) return discounts;

        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                HeaderColumnNameMappingStrategy<Discount> strategy = new HeaderColumnNameMappingStrategy<>();
                strategy.setType(Discount.class);

                List<Discount> fileDiscounts = new CsvToBeanBuilder<Discount>(reader)
                        .withType(Discount.class)
                        .withSeparator(';')
                        .withMappingStrategy(strategy)
                        .build()
                        .parse();

                String source = file.getName().toLowerCase().contains("kaufland") ? "kaufland"
                        : file.getName().toLowerCase().contains("lidl") ? "lidl"
                        : "profi";

                fileDiscounts.forEach(d -> d.setSource(source));
                discounts.addAll(fileDiscounts);

            } catch (Exception e) {
                System.err.println("Error reading file: " + file.getName());
                e.printStackTrace();
            }
        }

        return discounts;
    }
}
