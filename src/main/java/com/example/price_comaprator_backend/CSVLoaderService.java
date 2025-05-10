package com.example.price_comaprator_backend;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class CSVLoaderService {

    public List<Product> loadProductsFromCSV(String fileName) {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
            if (inputStream == null) {
                throw new RuntimeException("File not found: " + fileName);
            }

            var csvParser = new CSVParserBuilder()
                    .withSeparator(';')
                    .build();

            var csvReader = new CSVReaderBuilder(new InputStreamReader(inputStream))
                    .withCSVParser(csvParser)
                    .build();

            CsvToBean<Product> csvToBean = new CsvToBeanBuilder<Product>(csvReader)
                    .withType(Product.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<Product> products = csvToBean.parse();


            String source = fileName.split("_")[0].toLowerCase();

            for (Product product : products) {
                product.setSource(source);
            }

            return products;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load CSV file: " + e.getMessage(), e);
        }
    }

    public List<Product> loadAllCSVs(List<String> fileNames) {
        List<Product> allProducts = new ArrayList<>();
        for (String fileName : fileNames) {
            allProducts.addAll(loadProductsFromCSV(fileName));
        }
        return allProducts;
    }
}
