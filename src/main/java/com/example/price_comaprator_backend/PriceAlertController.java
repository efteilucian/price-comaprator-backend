package com.example.price_comaprator_backend;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class PriceAlertController {

    private static final Logger logger = LoggerFactory.getLogger(PriceAlertController.class); // Added logger
    private final PriceAlertService alertService;

    public PriceAlertController(PriceAlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping("/add")
    public ResponseEntity<String> createAlert(@RequestBody PriceAlert alert) {
        if (alert.getProductName() == null || alert.getProductName().isBlank() ||
                alert.getCurrency() == null || alert.getCurrency().isBlank()) {
            logger.warn("Received invalid alert creation request: Missing name, price, or currency.");
            return ResponseEntity.badRequest().body("❌ Product name, target price, and currency are required for an alert.");
        }
        alertService.addAlert(alert); // PriceAlertService now handles detailed logging
        return ResponseEntity.ok("✅ Alert added for " + alert.getProductName());
    }

    @GetMapping
    public List<PriceAlert> getAllAlerts() {
        return alertService.getAllAlerts();
    }

    @GetMapping("/check")
    public List<PriceAlert> checkAlerts() { // This now returns PriceAlert objects representing triggered alerts
        return alertService.checkAlerts();
    }
}