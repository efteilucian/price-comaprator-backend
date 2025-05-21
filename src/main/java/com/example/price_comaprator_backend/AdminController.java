package com.example.price_comaprator_backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
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
            logger.error("Error during product reload via admin endpoint: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error during product reload: " + e.getMessage());
        }
    }
}