package com.example.price_comaprator_backend;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class BasketClient {

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        ShoppingBasket basket = new ShoppingBasket();

        // ✅ Load products using CSVLoaderService
        CSVLoaderService loader = new CSVLoaderService();
        List<String> csvFileNames = List.of(
                "kaufland_2025-05-01.csv",
                "kaufland_2025-05-08.csv",
                "lidl_2025-05-01.csv",
                "lidl_2025-05-08.csv",
                "profi_2025-05-01.csv",
                "profi_2025-05-08.csv"
                // Add all your actual file names here
        );

        List<Product> products = loader.loadAllCSVs(csvFileNames);

        while (true) {
            System.out.println("\n=== MAIN MENU ===");
            System.out.println("1. View all products");
            System.out.println("2. Add item to basket");
            System.out.println("3. View basket");
            System.out.println("4. Optimize basket (best prices)");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.println("\nAvailable Products:");
                    products.stream()
                            .filter(p -> p.getProductName() != null)
                            .sorted(Comparator.comparing(Product::getProductName))
                            .limit(100)
                            .forEach(p -> System.out.printf("- %s (%s): %.2f %s [%s]%n",
                                    p.getProductName(), p.getBrand(), p.getPrice(),
                                    p.getCurrency(), p.getSource()));
                    break;

                case "2":
                    System.out.print("Enter product name: ");
                    String name = scanner.nextLine();

                    List<Product> matches = products.stream()
                            .filter(p -> p.getProductName() != null &&
                                    p.getProductName().equalsIgnoreCase(name))
                            .sorted(Comparator.comparing(Product::getPrice))
                            .toList();

                    if (matches.isEmpty()) {
                        System.out.println("❌ Product not found.");
                    } else {
                        System.out.print("Enter quantity: ");
                        int qty = Integer.parseInt(scanner.nextLine());

                        basket.getItems().add(new BasketItem(name, qty));
                        System.out.println("✅ Added to basket.");
                        System.out.println("Best options:");
                        matches.forEach(p -> System.out.printf("- %.2f %s at %s%n",
                                p.getPrice(), p.getCurrency(), p.getSource()));
                    }
                    break;

                case "3":
                    if (basket.getItems().isEmpty()) {
                        System.out.println("Basket is empty.");
                    } else {
                        System.out.println("Current basket:");
                        basket.getItems().forEach(i -> System.out.println("- " + i.getProductName() + " x" + i.getQuantity()));
                    }
                    break;

                case "4":
                    ObjectMapper mapper = new ObjectMapper();
                    String json = mapper.writeValueAsString(basket);

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(new URI("http://localhost:8080/api/optimize"))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(json))
                            .build();

                    HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                    System.out.println("\n=== Optimized Basket ===");
                    System.out.println(response.body());
                    break;

                case "5":
                    System.out.println("Exiting. Goodbye!");
                    return;

                default:
                    System.out.println("Invalid option.");
            }
        }
    }
}
