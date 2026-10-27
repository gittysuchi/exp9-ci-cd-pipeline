package com.exp9.cicd.config;

import com.exp9.cicd.model.Product;
import com.exp9.cicd.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/** Loads demo products at start-up (disabled in tests via app.seed.enabled=false). */
@Configuration
public class DataSeeder {

    @Bean
    @ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
    CommandLineRunner seedProducts(ProductRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.saveAll(List.of(
                        new Product("Laptop", 55000.0, 10),
                        new Product("Mechanical Keyboard", 3499.0, 3),
                        new Product("Wireless Mouse", 799.0, 25),
                        new Product("27-inch Monitor", 14999.0, 4),
                        new Product("USB-C Hub", 1299.0, 40)));
            }
        };
    }
}
