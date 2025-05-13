package com.example.price_comaprator_backend;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class BasketClient {

    private static final String OPTIMIZE_ENDPOINT = "http://localhost:8080/api/optimize";

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        ShoppingBasket basket = new ShoppingBasket();

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
                        System.out.print("Enter quantity: ");
                        try {
                            int qty = Integer.parseInt(scanner.nextLine().trim());
                            basket.getItems().add(new BasketItem(name, qty));
                            System.out.println("✅ Added to basket.");
                            System.out.println("Best options:");
                            matches.forEach(p -> System.out.printf("- %.2f %s at %s%n",
                                    p.getPrice(), p.getCurrency(), p.getSource()));
                        } catch (NumberFormatException e) {
                            System.out.println("❌ Invalid quantity.");
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
                    ObjectMapper mapper = new ObjectMapper();
                    String json = mapper.writeValueAsString(basket);

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(new URI(OPTIMIZE_ENDPOINT))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(json))
                            .build();

                    HttpResponse<String> response = HttpClient.newHttpClient()
                            .send(request, HttpResponse.BodyHandlers.ofString());

                    System.out.println("\n=== Optimized Basket ===");
                    System.out.println(response.body());
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
