package com.example.price_comaprator_backend;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class PriceAlertController {

    private final PriceAlertService alertService;

    public PriceAlertController(PriceAlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping("/add")
    public ResponseEntity<String> createAlert(@RequestBody PriceAlert alert) {
        alertService.addAlert(alert);
        return ResponseEntity.ok("✅ Alert added");
    }

    @GetMapping
    public List<PriceAlert> getAllAlerts() {
        return alertService.getAllAlerts();
    }

    @GetMapping("/check")
    public List<PriceAlert> checkAlerts() {
        return alertService.checkAlerts();
    }
}

