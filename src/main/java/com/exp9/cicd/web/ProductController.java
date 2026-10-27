package com.exp9.cicd.web;

import com.exp9.cicd.model.Product;
import com.exp9.cicd.model.ProductRequest;
import com.exp9.cicd.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public List<Product> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Product get(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/low-stock")
    public List<Product> lowStock(@RequestParam(defaultValue = "5") int threshold) {
        return service.findLowStock(threshold);
    }

    @GetMapping("/{id}/discount")
    public Map<String, Object> discount(@PathVariable Long id, @RequestParam double percent) {
        Product product = service.findById(id);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("productId", product.getId());
        body.put("originalPrice", product.getPrice());
        body.put("discountPercent", percent);
        body.put("finalPrice", service.priceWithDiscount(id, percent));
        return body;
    }

    @PostMapping
    public ResponseEntity<Product> create(@Valid @RequestBody ProductRequest request) {
        Product saved = service.create(request);
        return ResponseEntity.created(URI.create("/api/products/" + saved.getId())).body(saved);
    }
}
