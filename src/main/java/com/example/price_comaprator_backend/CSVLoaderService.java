package com.example.price_comaprator_backend;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
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

            return csvToBean.parse();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load CSV file: " + e.getMessage(), e);
        }
    }
}
