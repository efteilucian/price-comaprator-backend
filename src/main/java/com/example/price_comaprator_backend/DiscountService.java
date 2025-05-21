package com.example.price_comaprator_backend;

import com.opencsv.bean.CsvToBeanBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DiscountService {

    private static final Logger logger = LoggerFactory.getLogger(DiscountService.class);

    /**
     * Loads discount data from a list of specified CSV filenames.
     * Assumes CSV files are located in a "discounts/" subdirectory within the classpath resources.
     *
     * @param fileNames List of CSV file names to load.
     * @return A list of Discount objects parsed from the files.
     */
    public List<Discount> loadDiscounts(List<String> fileNames) {
        logger.info("DiscountService: Attempting to load discounts from {} files.", fileNames.size());
        List<Discount> discounts = new ArrayList<>();

        for (String fileName : fileNames) {
            String resourcePath = "discounts/" + fileName;
            logger.debug("DiscountService: Loading discounts from resource: {}", resourcePath);
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);

                 Reader reader = new InputStreamReader(Objects.requireNonNull(inputStream, "InputStream for " + resourcePath + " was null."), StandardCharsets.UTF_8)) {

                List<Discount> fileDiscounts = new CsvToBeanBuilder<Discount>(reader)
                        .withType(Discount.class)
                        .withIgnoreLeadingWhiteSpace(true)
                        .withSeparator(';')
                        .build()
                        .parse();

                // Extracts store name from filename (e.g., "kaufland" from "kaufland_discounts_...")
                String store = "unknown";
                if (fileName != null && fileName.contains("_")) {
                    store = fileName.split("_")[0].toLowerCase();
                } else {
                    logger.warn("DiscountService: Could not determine store from discount filename: {}. Defaulting to 'unknown'.", fileName);
                }

                for (Discount discount : fileDiscounts) {
                    discount.setSource(store);
                }

                discounts.addAll(fileDiscounts);
                logger.info("DiscountService: Successfully loaded {} discounts from {}.", fileDiscounts.size(), fileName);

            } catch (NullPointerException e) {
                logger.error("DiscountService: ❌ Discount resource not found (NullPointerException) at path: {}. Check if file exists and path is correct. Error: {}", resourcePath, e.getMessage());
            }
            catch (Exception e) {
                logger.error("DiscountService: ❌ Failed to load or parse discounts from {}: {}", resourcePath, e.getMessage(), e);
            }
        }
        logger.info("DiscountService: Finished loading discounts from all specified files. Total loaded: {}.", discounts.size());
        return discounts;
    }

    /**
     * Finds discounts from a list of all available discounts that are applicable to items in a given basket.
     * Matching is based on a case-insensitive comparison of product name, brand, and source (store).
     *
     * @param basket       The list of items in the user's basket.
     * @param allDiscounts The complete list of all available discounts.
     * @return A list of Discount objects that match items in the basket.
     */
    public List<Discount> findDiscountsForBasket(List<BasketItem> basket, List<Discount> allDiscounts) {
        logger.info("Finding discounts for basket with {} item types, from a pool of {} discounts.", basket.size(), allDiscounts.size());
        List<Discount> matched = new ArrayList<>();
        if (basket.isEmpty() || allDiscounts.isEmpty()) {
            logger.debug("Basket or global discount list is empty, no discounts matched.");
            return matched;
        }

        for (BasketItem item : basket) {
            String basketProductName = item.getProductName().trim().toLowerCase();
            String basketBrand = (item.getBrand() != null) ? item.getBrand().trim().toLowerCase() : "";
            String basketSource = (item.getSource() != null) ? item.getSource().trim().toLowerCase() : "";

            logger.trace("Checking discounts for basket item: Name='{}', Brand='{}', Source='{}'", basketProductName, basketBrand, basketSource);

            for (Discount discount : allDiscounts) {

                if (discount.getProductName() != null && discount.getBrand() != null && discount.getSource() != null) {
                    String discountProductName = discount.getProductName().trim().toLowerCase();
                    String discountBrand = discount.getBrand().trim().toLowerCase();
                    String discountSource = discount.getSource().trim().toLowerCase();

                    if (basketProductName.equals(discountProductName) &&
                            basketBrand.equals(discountBrand) &&
                            basketSource.equals(discountSource)) {
                        logger.debug("Match found for basket item '{}': Discount on '{}' from '{}'", item.getProductName(), discount.getProductName(), discount.getSource());
                        matched.add(discount);

                    }
                }
            }
        }
        logger.info("Found {} discounts applicable to the basket items.", matched.size());
        return matched;
    }

    /**
     * Retrieves the top N discounts based on the highest percentage off.
     * If multiple discounts exist for the same product (defined by name + brand),
     * only the one with the highest percentage discount is considered among them.
     *
     * @param allDiscounts The list of all discounts to consider.
     * @param limit        The maximum number of best discounts to return.
     * @return A list of Discount objects representing the best discounts, sorted by percentage.
     */
    public List<Discount> getBestDiscounts(List<Discount> allDiscounts, int limit) {
        logger.info("Getting best {} discounts from a pool of {} total discounts.", limit, allDiscounts.size());
        if (allDiscounts.isEmpty()) {
            return Collections.emptyList();
        }


        Map<String, Discount> bestPerProduct = new HashMap<>();
        for (Discount d : allDiscounts) {
            if (d.getProductName() == null || d.getBrand() == null) {
                logger.trace("Skipping discount with null name or brand while finding best: {}", d);
                continue;
            }
            String key = (d.getProductName() + "_" + d.getBrand()).toLowerCase().trim();


            bestPerProduct.merge(key, d, (oldD, newD) -> newD.getPercentageOfDiscount() > oldD.getPercentageOfDiscount() ? newD : oldD);
        }

        List<Discount> topDiscounts = bestPerProduct.values().stream()
                .sorted(Comparator.comparingInt(Discount::getPercentageOfDiscount).reversed()) 
                .limit(limit)
                .collect(Collectors.toList());

        logger.info("Returning {} top discounts.", topDiscounts.size());
        return topDiscounts;
    }

    /**
     * Finds discounts that are considered "new" based on their start date ('fromDate').
     * A discount is new if its 'fromDate' is not before the date calculated by subtracting 'days' from the current date.
     *
     * @param allDiscounts The list of all discounts to filter.
     * @param days         The number of past days to consider for newness (e.g., 1 for "today", 7 for "last week").
     * @return A list of new Discount objects.
     */
    public List<Discount> findNewDiscounts(List<Discount> allDiscounts, int days) {
        logger.info("Finding new discounts with a start date within the last {} day(s).", days);
        if (allDiscounts.isEmpty()) {
            return Collections.emptyList();
        }
        LocalDate cutoffDate = LocalDate.now().minusDays(days);

        List<Discount> newDiscountsList = allDiscounts.stream()
                .filter(d -> d.getFromDate() != null && !d.getFromDate().isBefore(cutoffDate))
                .collect(Collectors.toList());
        logger.info("Found {} new discounts starting on or after {}.", newDiscountsList.size(), cutoffDate.plusDays(1));
        return newDiscountsList;
    }
}