package com.example.price_comaprator_backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final CSVLoaderService csvLoaderService;

    @Autowired
    public ProductController(CSVLoaderService csvLoaderService) {
        this.csvLoaderService = csvLoaderService;
    }

    @GetMapping
    public List<Product> getAllProducts() {

        List<String> files = List.of(
                "kaufland_2025-05-01.csv",
                "lidl_2025-05-01.csv",
                "profi_2025-05-01.csv"
        );
        return csvLoaderService.loadAllCSVs(files);
    }
}
