package com.example.price_comaprator_backend;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


@Service
public class CSVLoaderService {

    private static final Logger logger = LoggerFactory.getLogger(CSVLoaderService.class);

    public List<Product> loadProductsFromCSV(String fileName) {
        logger.debug("CSVLoaderService: Attempting to load products from CSV file: {}", fileName);
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);

            if (inputStream == null) {
                logger.error("CSVLoaderService: Resource file not found: {}. Ensure it's in src/main/resources.", fileName);
                throw new RuntimeException("Resource file not found: " + fileName);
            }


            try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {

                var csvParser = new CSVParserBuilder()
                        .withSeparator(';')
                        .build();


                var csvReader = new CSVReaderBuilder(reader)
                        .withCSVParser(csvParser)

                        .build();

                CsvToBean<Product> csvToBean = new CsvToBeanBuilder<Product>(csvReader)
                        .withType(Product.class)
                        .withIgnoreLeadingWhiteSpace(true)
                        .build();

                List<Product> products = csvToBean.parse();

                // Extracting the source (store name) from the filename.
                String source = "unknown";
                if (fileName != null && fileName.contains("_")) {
                    source = fileName.split("_")[0].toLowerCase();
                } else {
                    logger.warn("CSVLoaderService: Could not determine source from filename: {}. Defaulting to 'unknown'.", fileName);
                }


                for (Product product : products) {
                    product.setSource(source);

                }
                logger.info("CSVLoaderService: Successfully loaded {} products from {}.", products.size(), fileName);
                return products;

            }

        } catch (Exception e) {
            logger.error("CSVLoaderService: Failed to load or parse CSV file '{}': {}", fileName, e.getMessage(), e);
            throw new RuntimeException("Failed to load or parse CSV file '" + fileName + "': " + e.getMessage(), e);
        }
    }

    public List<Product> loadAllCSVs(List<String> fileNames) {
        logger.info("CSVLoaderService: Loading products from {} CSV files.", fileNames.size());
        List<Product> allProducts = new ArrayList<>();
        for (String fileName : fileNames) {
            try {
                allProducts.addAll(loadProductsFromCSV(fileName));
            } catch (RuntimeException e) {
                logger.error("CSVLoaderService: Failed to load products from {} during batch load. Continuing with other files. Error: {}", fileName, e.getMessage());
            }
        }
        logger.info("CSVLoaderService: Finished loading all specified CSV files. Total products loaded: {}.", allProducts.size());
        return allProducts;
    }
}