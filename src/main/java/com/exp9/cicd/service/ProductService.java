package com.exp9.cicd.service;

import com.exp9.cicd.model.Product;
import com.exp9.cicd.model.ProductRequest;
import com.exp9.cicd.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Business logic - this is the layer covered by the UNIT tests. */
@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public Product create(ProductRequest request) {
        return repository.save(new Product(request.name(), request.price(), request.stock()));
    }

    public List<Product> findAll() {
        return repository.findAll();
    }

    public Product findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    public List<Product> findLowStock(int threshold) {
        return repository.findByStockLessThan(threshold);
    }

    /** Price after applying a percentage discount, rounded to 2 decimals. */
    public double priceWithDiscount(Long id, double percent) {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("discount percent must be between 0 and 100");
        }
        Product product = findById(id);
        return BigDecimal.valueOf(product.getPrice())
                .multiply(BigDecimal.valueOf(100 - percent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
