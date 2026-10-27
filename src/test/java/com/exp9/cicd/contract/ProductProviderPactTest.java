package com.exp9.cicd.contract;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider;
import com.exp9.cicd.model.Product;
import com.exp9.cicd.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * CONTRACT TEST - PROVIDER SIDE.
 * Starts the REAL backend on a random port and replays every request recorded in the
 * consumer's pact file against it. If the backend ever changes in a way that would break
 * the frontend (renamed field, different status code...), THIS test fails in CI.
 */
@Provider("product-service")
@PactFolder("src/test/resources/pacts")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Contract test (provider) - backend honours the frontend contract")
class ProductProviderPactTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ProductRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void setTarget(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", port));
    }

    @TestTemplate
    @ExtendWith(PactVerificationSpringProvider.class)
    void verifyPact(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @State("product with id 1 exists")
    void productOneExists() {
        repository.deleteAll();
        jdbc.execute("ALTER TABLE products ALTER COLUMN id RESTART WITH 1");
        repository.save(new Product("Laptop", 55000.0, 10));
    }

    @State("no product with id 999 exists")
    void productNineNineNineMissing() {
        repository.deleteAll();
    }
}
