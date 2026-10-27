package com.example.exp9;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {
    public List<Product> findAll() {
        return List.of(new Product(1, "Notebook", 45.0), new Product(2, "Pen", 10.0));
    }

    public Product findById(long id) {
        return findAll().stream()
                .filter(product -> product.id() == id)
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
