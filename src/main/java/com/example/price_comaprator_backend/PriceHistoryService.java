package com.example.price_comaprator_backend;

import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.*;


@Service
public class PriceHistoryService {

    private final List<String> csvFilenames = List.of(
            "altex_2025-05-20.csv",
            "emag_2025-05-20.csv",
            "kaufland_2025-05-01.csv",
            "kaufland_2025-05-08.csv",
            "lidl_2025-05-01.csv",
            "lidl_2025-05-08.csv",
            "profi_2025-05-01.csv",
            "profi_2025-05-08.csv"
    );

    public List<PriceHistoryEntry> getPriceHistory(String productName, String brand, String store, String category) {
        List<PriceHistoryEntry> entries = new ArrayList<>();

        for (String filename : csvFilenames) {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(filename)) {
                if (is == null) continue;

                List<Product> products = new CsvToBeanBuilder<Product>(new InputStreamReader(is))
                        .withType(Product.class)
                        .withSeparator(';')
                        .build()
                        .parse();

                for (Product p : products) {
                    if (p.getProductName() == null || p.getPrice() == null) continue;

                    boolean matches = (productName == null || normalize(p.getProductName()).contains(normalize(productName))) &&
                            (brand == null || p.getBrand() != null && p.getBrand().equalsIgnoreCase(brand)) &&
                            (store == null || filename.contains(store)) &&
                            (category == null || p.getProductCategory() != null && p.getProductCategory().equalsIgnoreCase(category));

                    if (matches) {
                        entries.add(new PriceHistoryEntry(
                                p.getProductName(),
                                p.getBrand(),
                                extractStoreName(filename),
                                p.getProductCategory(),
                                extractDate(filename),
                                p.getPrice()
                        ));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return entries;
    }

    private static String extractStoreName(String filename) {
        return filename.split("_")[0];
    }

    private static LocalDate extractDate(String filename) {
        String datePart = filename.replaceAll("[^0-9\\-]", "").substring(0, 10);
        return LocalDate.parse(datePart);
    }

    private static String normalize(String s) {
        return s == null ? "" : s.toLowerCase().replaceAll("[^a-z0-9 ]", "").trim();
    }
}
