# price-comparator-backend
Java,Spring,API


This project provides a suite of features for comparing product prices from various retailers, optimizing shopping baskets, managing discounts, setting price alerts, viewing price history, and getting product recommendations. The data is primarily sourced from CSV files.

## 🚀 Features

*   **Product Data Management:**
    *   Loads product information (name, brand, price, category, package details, currency, source store) from multiple CSV files.
    *   Standardizes product measurements (e.g., price per kg/liter/item) using a `UnitConverter` for fair comparisons across different packaging.
    *   Admin endpoint (`/api/admin/products/reload`) to refresh product data on-the-fly without restarting the application.
*   **Shopping Basket Functionality:**
    *   Add items to a session-based shopping basket (`/api/basket/add`).
    *   View current basket contents (`/api/basket`).
    *   Remove items from the basket (`/api/basket/remove`).
    *   Clear the entire basket (`/api/basket/clear`).
*   **Basket Optimization:**
    *   Optimizes a given shopping basket to find the cheapest options for each product across different stores (`/api/optimize` or `/api/basket/optimize`).
    *   Splits the optimized basket into per-store shopping lists, showing total cost per store.
    *   Uses string normalization and token-based similarity (Jaccard index) for robust product matching.
*   **Discount Management:**
    *   Loads discount information from dedicated CSV files.
    *   Finds discounts applicable to items currently in the user's basket (`/api/basket/discounts`).
    *   Displays the best available discounts across all products, sorted by percentage off (`/api/discounts/best`).
    *   Identifies newly added discounts within a specified number of days (`/api/basket/discounts/new`).
*   **Price Alerts:**
    *   Users can set price alerts for specific products at a target price (`/api/alerts/add`).
    *   The system can check current product prices against active alerts and notify if a target price is met or beaten (`/api/alerts/check`).
*   **Price History:**
    *   View historical price data for products based on various filters like product name, brand, store, or category (`/api/history`).
    *   Data is aggregated from dated CSV files.
*   **Product Recommendations:**
    *   Suggests "better value" alternatives for a given product based on its price per standard unit (`/api/recommendations`).
    *   Considers products in the same category and unit type (e.g., weight, volume, count).
*   **Command-Line Client (`BasketClient.java`):**
    *   A simple CLI demonstrating some of the core functionalities like viewing products, adding to a local basket, and seeing relevant discounts. This client interacts directly with services, not the HTTP API.
*   **Robust Logging:**
    *   Utilizes SLF4J for detailed logging across services and controllers, aiding in debugging and monitoring.

## 🛠️ Technologies Used

*   **Java 17+** (inferred from modern syntax and features)
*   **Spring Boot 3.x** (common choice for new Java backend projects)
    *   Spring Web (for RESTful APIs)
    *   Spring Actuator (implicitly, for application management - though not explicitly configured in provided code)
*   **Maven** (for project build and dependency management)
*   **OpenCSV** (for parsing product and discount data from CSV files)
*   **SLF4J & Logback** (for logging)

## ⚙️ Setup and Running

### Prerequisites

*   Java Development Kit (JDK) 17 or higher
*   Apache Maven

### Steps

1.  **Clone the repository:**
    ```bash
    git clone <your-repository-url>
    cd price_comaprator_backend
    ```

2.  **Data Files:**
    *   Ensure your product CSV files (e.g., `kaufland_2025-05-01.csv`, `emag_2025-05-20.csv`) are placed in the `src/main/resources/` directory.
    *   Ensure your discount CSV files (e.g., `kaufland_discounts_2025-05-01.csv`) are placed in the `src/main/resources/discounts/` directory.
    *   The application expects filenames to follow a pattern like `storename_YYYY-MM-DD.csv` for products and `storename_discounts_YYYY-MM-DD.csv` for discounts.

3.  **Build the application:**
    ```bash
    mvn clean install
    ```

4.  **Run the application:**
    ```bash
    java -jar target/price_comaprator_backend-0.0.1-SNAPSHOT.jar
    ```
    (The JAR filename might vary slightly based on your `pom.xml` configuration).

    The application will start, typically on `http://localhost:8080`.

## 📖 API Endpoints

The base URL for the API is `http://localhost:8080/api`.

### Admin
*   `POST /api/admin/products/reload`
    *   Reloads all product data from CSV files.
    *   **Response:** Success message with timestamp and total products loaded, or an error message.

### Basket
*   `POST /api/basket/add`
    *   Adds an item to the current session's shopping basket.
    *   **Request Body:** `BasketItem` JSON (e.g., `{"productName": "Lapte Zuzu", "brand": "Zuzu", "quantity": 2, "source": "kaufland"}`)
    *   **Response:** Success or error message.
*   `GET /api/basket`
    *   Retrieves all items in the current session's shopping basket.
    *   **Response:** List of `BasketItem` objects.
*   `DELETE /api/basket/remove`
    *   Removes a specific item from the basket.
    *   **Request Body:** `BasketItem` JSON (matching `productName`, `brand`, `source`).
    *   **Response:** Success or error message.
*   `GET /api/basket/discounts`
    *   Gets discounts applicable to the items in the current basket.
    *   **Response:** List of `Discount` objects.
*   `GET /api/basket/discounts/new?days={count}`
    *   Gets discounts that started within the last `count` days (default: 1).
    *   **Response:** List of `Discount` objects.
*   `GET /api/basket/optimize`
    *   Optimizes the current session's basket and returns per-store shopping lists.
    *   **Response:** List of `OptimizedShoppingList` objects.
*   `POST /api/basket/clear`
    *   Clears all items from the current session's basket.
    *   **Response:** Success message.

### Basket Optimization (Direct)
*   `POST /api/optimize`
    *   Optimizes a shopping basket provided in the request body.
    *   **Request Body:** `ShoppingBasket` JSON (containing a list of `BasketItem`s).
    *   **Response:** List of `OptimizedShoppingList` objects.

### Discounts
*   `GET /api/discounts/best`
    *   Retrieves the top 10 best discounts available across all products (highest percentage off).
    *   **Response:** List of `Discount` objects.

### Price Alerts
*   `POST /api/alerts/add`
    *   Adds a new price alert for a product.
    *   **Request Body:** `PriceAlert` JSON (e.g., `{"productName": "iPhone 15", "targetPrice": 4000, "currency": "RON"}`)
    *   **Response:** Success or error message.
*   `GET /api/alerts`
    *   Retrieves all currently set price alerts.
    *   **Response:** List of `PriceAlert` objects (as defined by user).
*   `GET /api/alerts/check`
    *   Checks all active alerts against current product prices.
    *   **Response:** List of `PriceAlert` objects representing triggered alerts (includes `currentPriceOnMatch` and `storeOnMatch`).

### Price History
*   `GET /api/history?productName={name}&brand={brand}&store={store}&category={category}`
    *   Retrieves price history entries. All query parameters are optional.
    *   **Response:** List of `PriceHistoryEntry` objects.

### Recommendations
*   `GET /api/recommendations?productName={name}`
    *   Suggests better value alternatives for the given product name.
    *   **Response:** List of `Product` objects.

## 📁 Project Structure (High-Level)

*   `com.example.price_comaprator_backend`
    *   **`AdminController.java`**: Handles administrative tasks like reloading product data.
    *   **`Basket.java`**: Represents a simple list of basket items for session management.
    *   **`BasketClient.java`**: A command-line interface for demonstrating/testing some functionalities locally.
    *   **`BasketController.java`**: Manages user's shopping basket via API endpoints.
    *   **`BasketItem.java`**: DTO for items in a shopping basket.
    *   **`BasketOptimizationController.java`**: API endpoint for direct basket optimization requests.
    *   **`BasketOptimizationService.java`**: Core logic for product loading, normalization, and basket optimization.
    *   **`CSVLoaderService.java`**: Service responsible for loading `Product` data from CSV files.
    *   **`Discount.java`**: DTO for discount information.
    *   **`DiscountController.java`**: API endpoints related to global discount queries.
    *   **`DiscountService.java`**: Service for loading and processing discount data.
    *   **`OptimizedBasketItem.java`**: DTO for an item within an optimized shopping list.
    *   **`OptimizedShoppingList.java`**: DTO representing a shopping list for a specific store after optimization.
    *   **`PriceAlert.java`**: DTO for price alert definitions and matches.
    *   **`PriceAlertController.java`**: API endpoints for managing price alerts.
    *   **`PriceAlertMatch.java`**: (Note: This class was present but `PriceAlert` DTO was enhanced to include match details directly, making this potentially redundant for API responses. `PriceAlertService` returns `PriceAlert` objects for matches).
    *   **`PriceAlertService.java`**: Service for managing and checking price alerts.
    *   **`PriceComapratorBackendApplication.java`**: Main Spring Boot application class.
    *   **`PriceHistoryController.java`**: API endpoints for querying price history.
    *   **`PriceHistoryEntry.java`**: DTO for price history records.
    *   **`PriceHistoryService.java`**: Service for retrieving price history from CSV data.
    *   **`Product.java`**: DTO for product information, including logic for standardizing units and calculating price per standard unit.
    *   **`RecommendationController.java`**: API endpoints for product recommendations.
    *   **`RecommendationService.java`**: Service for generating product recommendations.
    *   **`ShoppingBasket.java`**: DTO used for basket optimization requests, containing a list of `BasketItem`s.
    *   **`UnitConverter.java`**: Utility class for converting product units (e.g., grams to kilograms, ml to liters) and determining base unit types.

## 💡 Future Enhancements (Potential Ideas)

*   User authentication and persistent baskets per user.
*   Database integration for storing products, discounts, and alerts instead of relying solely on CSVs.
*   More sophisticated product matching algorithms.
*   Web scraping capabilities to fetch real-time prices.
*   Frontend UI to interact with the backend.
*   Notification system for price alerts (e.g., email, push notifications).

---

This project demonstrates practical application of Java, Spring Boot, and data processing techniques. It showcases the ability to design and implement a multi-featured backend system.
