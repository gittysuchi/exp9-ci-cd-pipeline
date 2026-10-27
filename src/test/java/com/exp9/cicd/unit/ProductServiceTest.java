package com.exp9.cicd.unit;

import com.exp9.cicd.model.Product;
import com.exp9.cicd.model.ProductRequest;
import com.exp9.cicd.repository.ProductRepository;
import com.exp9.cicd.service.ProductNotFoundException;
import com.exp9.cicd.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** UNIT TESTS - JUnit 5 + Mockito. The repository is mocked, so no Spring context and no database. */
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit tests - ProductService")
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService service;

    @Test
    @DisplayName("create() saves a product built from the request")
    void createSavesProduct() {
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product saved = service.create(new ProductRequest("Mouse", 799.0, 25));

        assertEquals("Mouse", saved.getName());
        assertEquals(799.0, saved.getPrice());
        assertEquals(25, saved.getStock());
        verify(repository).save(any(Product.class));
    }

    @Test
    @DisplayName("findById() returns the product when it exists")
    void findByIdReturnsProduct() {
        Product laptop = new Product("Laptop", 55000.0, 10);
        when(repository.findById(1L)).thenReturn(Optional.of(laptop));

        assertEquals("Laptop", service.findById(1L).getName());
    }

    @Test
    @DisplayName("findById() throws ProductNotFoundException when missing")
    void findByIdThrowsWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> service.findById(99L));
    }

    @ParameterizedTest(name = "price {0} with {1}% discount -> {2}")
    @CsvSource({
            "1000.0, 10, 900.0",
            "2000.0, 25, 1500.0",
            "499.99, 0, 499.99",
            "100.0, 100, 0.0",
            "333.33, 10, 300.0"
    })
    @DisplayName("priceWithDiscount() applies the percentage and rounds to 2 decimals")
    void discountIsCalculatedCorrectly(double price, double percent, double expected) {
        when(repository.findById(1L)).thenReturn(Optional.of(new Product("Item", price, 1)));

        assertEquals(expected, service.priceWithDiscount(1L, percent), 0.001);
    }

    @ParameterizedTest(name = "discount {0}% is rejected")
    @ValueSource(doubles = {-1, -0.5, 100.5, 250})
    @DisplayName("priceWithDiscount() rejects percentages outside 0-100")
    void invalidDiscountIsRejected(double percent) {
        assertThrows(IllegalArgumentException.class, () -> service.priceWithDiscount(1L, percent));
        verify(repository, never()).findById(any());
    }

    @Test
    @DisplayName("findLowStock() delegates to the repository with the threshold")
    void lowStockDelegatesToRepository() {
        Product keyboard = new Product("Keyboard", 3499.0, 3);
        when(repository.findByStockLessThan(5)).thenReturn(List.of(keyboard));

        List<Product> result = service.findLowStock(5);

        assertEquals(1, result.size());
        assertEquals("Keyboard", result.get(0).getName());
    }
}
