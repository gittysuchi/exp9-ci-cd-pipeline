package com.exp9.cicd.client;

import com.exp9.cicd.model.Product;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * The "frontend" side of the contract - a consumer of the Product service.
 * The Pact consumer test records what this client expects from the backend.
 */
public class ProductClient {

    private final RestTemplate rest;

    public ProductClient(String baseUrl) {
        this.rest = new RestTemplateBuilder().rootUri(baseUrl).build();
    }

    public Product getProduct(long id) {
        return rest.getForObject("/api/products/{id}", Product.class, id);
    }

    public List<Product> getAllProducts() {
        Product[] products = rest.getForObject("/api/products", Product[].class);
        return products == null ? List.of() : Arrays.asList(products);
    }
}
