package com.exp9.cicd.containers;

import com.exp9.cicd.model.Product;
import com.exp9.cicd.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TESTCONTAINERS - starts a REAL PostgreSQL 16 database in a throw-away Docker container.
 * @ServiceConnection tells Spring Boot to point the DataSource at the container automatically.
 *
 * If Docker is not running on your machine the test is SKIPPED (not failed).
 * On GitHub Actions (ubuntu-latest) Docker is always available, so it really runs there.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@DisplayName("Testcontainers - repository against real PostgreSQL")
class ProductPostgresContainerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ProductRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("the container is up and PostgreSQL is the database in use")
    void containerIsRunning() {
        assertTrue(postgres.isRunning());
        assertTrue(postgres.getJdbcUrl().startsWith("jdbc:postgresql://"));
    }

    @Test
    @DisplayName("save + findById round-trips through PostgreSQL")
    void saveAndFind() {
        Product saved = repository.save(new Product("Laptop", 55000.0, 10));

        assertNotNull(saved.getId());
        assertEquals("Laptop", repository.findById(saved.getId()).orElseThrow().getName());
    }

    @Test
    @DisplayName("findByStockLessThan() runs as real SQL")
    void lowStockQuery() {
        repository.save(new Product("Keyboard", 3499.0, 3));
        repository.save(new Product("Mouse", 799.0, 25));

        List<Product> low = repository.findByStockLessThan(5);

        assertEquals(1, low.size());
        assertEquals("Keyboard", low.get(0).getName());
    }
}
