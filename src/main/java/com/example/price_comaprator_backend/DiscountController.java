package com.example.price_comaprator_backend;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;


@RestController
@RequestMapping("/api/discounts")
public class DiscountController {

    private final DiscountService discountService;
    private final List<Discount> allDiscounts;

    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
        // Load once at startup
        this.allDiscounts = discountService.loadDiscounts(List.of(
                "altex_discounts-2025-05-20.csv",
                "emag_discounts_2025-05-20.csv",
                "kaufland_discounts_2025-05-01.csv",
                "kaufland_discounts_2025-05-08.csv",
                "lidl_discounts_2025-05-01.csv",
                "lidl_discounts_2025-05-08.csv",
                "profi_discounts_2025-05-01.csv",
                "profi_discounts_2025-05-08.csv"
        ));
    }

    @GetMapping("/best")
    public List<Discount> getBestDiscounts() {
        return discountService.getBestDiscounts(allDiscounts, 10);
    }
}
