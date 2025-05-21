package com.example.price_comaprator_backend;

import org.slf4j.Logger; // Added for potential error logging
import org.slf4j.LoggerFactory; // Added
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class); // Added logger
    private final BasketOptimizationService basketOptimizationService;

    public AdminController(BasketOptimizationService basketOptimizationService) {
        this.basketOptimizationService = basketOptimizationService;
    }

    @PostMapping("/products/reload")
    public ResponseEntity<String> reloadProducts() {
        try {
            basketOptimizationService.refreshProducts();
            return ResponseEntity.ok("Product data reloaded successfully at " + LocalDateTime.now() +
                    ". Total products: " + basketOptimizationService.getAllProducts().size());
        } catch (Exception e) {
            logger.error("Error during product reload via admin endpoint: {}", e.getMessage(), e); // Added specific logging for errors
            return ResponseEntity.status(500).body("Error during product reload: " + e.getMessage());
        }
    }
}