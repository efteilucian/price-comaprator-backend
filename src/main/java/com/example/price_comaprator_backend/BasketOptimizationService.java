package com.example.price_comaprator_backend;

import com.opencsv.bean.CsvToBeanBuilder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.util.*;

@Service
public class BasketOptimizationService {

    private final String dataDirectory = "src/main/resources";

    public List<OptimizedBasketItem> optimizeBasket(ShoppingBasket basket) {
        List<Product> allProducts = loadAllProducts();
        List<OptimizedBasketItem> optimizedList = new ArrayList<>();

        for (BasketItem item : basket.getItems()) {
            Optional<Product> cheapestMatch = allProducts.stream()
                    .filter(p -> p.getProductName().toLowerCase().contains(item.getProductName().toLowerCase()))
                    .min(Comparator.comparing(Product::getPrice));

            cheapestMatch.ifPresent(product -> optimizedList.add(new OptimizedBasketItem(
                    product.getProductName(),
                    item.getQuantity(),
                    product.getSource(),
                    product.getPrice()
            )));
        }

        return optimizedList;
    }


    private List<Product> loadAllProducts() {
        List<Product> products = new ArrayList<>();
        File folder = new File(dataDirectory);

        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.getName().endsWith(".csv")) {
                try (FileReader reader = new FileReader(file)) {
                    List<Product> fileProducts = new CsvToBeanBuilder<Product>(reader)
                            .withType(Product.class)
                            .build()
                            .parse();
                    fileProducts.forEach(p -> p.setSource(file.getName()));
                    products.addAll(fileProducts);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return products;
    }
}
