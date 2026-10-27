package com.example.exp9;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTest {
    private final ProductService service = new ProductService();

    @Test
    void returnsProducts() {
        assertEquals(2, service.findAll().size());
        assertEquals("Notebook", service.findAll().get(0).name());
    }

    @Test
    void findsProductById() {
        assertEquals(10.0, service.findById(2).price());
    }

    @Test
    void throwsWhenProductDoesNotExist() {
        assertThrows(ProductNotFoundException.class, () -> service.findById(99));
    }
}
