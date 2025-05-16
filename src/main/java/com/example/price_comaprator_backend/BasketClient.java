package com.example.price_comaprator_backend;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

public class BasketClient {

    private static final String OPTIMIZE_ENDPOINT = "http://localhost:8080/api/optimize";

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        ShoppingBasket basket = new ShoppingBasket();
        DiscountService discountService = new DiscountService();
        CSVLoaderService loader = new CSVLoaderService();

        List<String> csvFileNames = List.of(
                "kaufland_2025-05-01.csv", "kaufland_2025-05-08.csv",
                "lidl_2025-05-01.csv", "lidl_2025-05-08.csv",
                "profi_2025-05-01.csv", "profi_2025-05-08.csv"
        );

        List<Product> products = loader.loadAllCSVs(csvFileNames);

        while (true) {
            System.out.println("\n=== MAIN MENU ===");
            System.out.println("1. View all products");
            System.out.println("2. Add item to basket");
            System.out.println("3. View basket");
            System.out.println("4. Show Discounts");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    System.out.println("\nAvailable Products:");
                    products.stream()
                            .filter(p -> p.getProductName() != null)
                            .sorted(Comparator.comparing(Product::getProductName))
                            .limit(100)
                            .forEach(p -> System.out.printf("- %s (%s): %.2f %s [%s]%n",
                                    p.getProductName(), p.getBrand(), p.getPrice(),
                                    p.getCurrency(), p.getSource()));
                }

                case "2" -> {
                    System.out.print("Enter product name: ");
                    String name = scanner.nextLine().trim();

                    List<Product> matches = products.stream()
                            .filter(p -> p.getProductName() != null &&
                                    p.getProductName().equalsIgnoreCase(name))
                            .sorted(Comparator.comparing(Product::getPrice))
                            .toList();

                    if (matches.isEmpty()) {
                        System.out.println("❌ Product not found.");
                    } else {
                        System.out.println("Available options:");
                        for (int i = 0; i < matches.size(); i++) {
                            Product p = matches.get(i);
                            System.out.printf("%d. %s (%s) - %.2f %s at %s%n",
                                    i + 1, p.getProductName(), p.getBrand(), p.getPrice(),
                                    p.getCurrency(), p.getSource());
                        }

                        System.out.print("Select option by number: ");
                        try {
                            int option = Integer.parseInt(scanner.nextLine().trim());
                            if (option < 1 || option > matches.size()) {
                                System.out.println("❌ Invalid selection.");
                                break;
                            }
                            Product selected = matches.get(option - 1);

                            System.out.print("Enter quantity: ");
                            int qty = Integer.parseInt(scanner.nextLine().trim());

                            basket.getItems().add(new BasketItem(selected.getProductName(), selected.getBrand(), qty));
                            System.out.println("✅ Added to basket.");
                        } catch (NumberFormatException e) {
                            System.out.println("❌ Invalid input.");
                        }
                    }
                }


                case "3" -> {
                    if (basket.getItems().isEmpty()) {
                        System.out.println("🛒 Basket is empty.");
                    } else {
                        System.out.println("🧾 Current basket:");
                        basket.getItems().forEach(i ->
                                System.out.printf("- %s x%d%n", i.getProductName(), i.getQuantity()));
                    }
                }

                case "4" -> {
                    List<String> discountFileNames = List.of(
                            "kaufland_discounts_2025-05-01.csv",
                            "kaufland_discounts_2025-05-08.csv",
                            "lidl_discounts_2025-05-01.csv",
                            "lidl_discounts_2025-05-08.csv",
                            "profi_discounts_2025-05-01.csv",
                            "profi_discounts_2025-05-08.csv"
                    );

                    List<Discount> allDiscounts = discountService.loadDiscounts(discountFileNames);
                    List<Discount> relevantDiscounts = discountService.findDiscountsForBasket(basket.getItems(), allDiscounts);

                    if (relevantDiscounts.isEmpty()) {
                        System.out.println("ℹ️ No discounts available for these products.");
                    } else {
                        System.out.println("\n=== Discounts for Your Basket ===");
                        relevantDiscounts.forEach(d -> System.out.printf(
                                "- %s (%s): %d%% off at %s from %s to %s%n",
                                d.getProductName(), d.getBrand(), d.getPercentageOfDiscount(),d.getSource(),
                                 d.getFromDate(), d.getToDate()
                        ));
                    }
                }

                case "5" -> {
                    System.out.println("👋 Exiting. Goodbye!");
                    return;
                }

                default -> System.out.println("⚠️ Invalid option.");
            }
        }
    }
}
