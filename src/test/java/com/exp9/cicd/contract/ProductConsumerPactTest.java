package com.exp9.cicd.contract;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.exp9.cicd.client.ProductClient;
import com.exp9.cicd.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * CONTRACT TEST - CONSUMER SIDE.
 * The "frontend" (ProductClient) states exactly what it needs from the backend.
 * Running this test writes the contract file  src/test/resources/pacts/product-frontend-product-service.json
 * which the provider (backend) must then satisfy - see ProductProviderPactTest.
 */
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "product-service", pactVersion = PactSpecVersion.V3)
@DisplayName("Contract test (consumer) - frontend expectations")
class ProductConsumerPactTest {

    @Pact(consumer = "product-frontend")
    public RequestResponsePact getSingleProductPact(PactDslWithProvider builder) {
        return builder
                .given("product with id 1 exists")
                .uponReceiving("a request for product 1")
                .path("/api/products/1")
                .method("GET")
                .willRespondWith()
                .status(200)
                .matchHeader("Content-Type", "application/json.*", "application/json")
                .body(new PactDslJsonBody()
                        .integerType("id", 1)
                        .stringType("name", "Laptop")
                        .numberType("price", 55000.0)
                        .integerType("stock", 10))
                .toPact();
    }

    @Pact(consumer = "product-frontend")
    public RequestResponsePact getAllProductsPact(PactDslWithProvider builder) {
        return builder
                .given("product with id 1 exists")
                .uponReceiving("a request for all products")
                .path("/api/products")
                .method("GET")
                .willRespondWith()
                .status(200)
                .matchHeader("Content-Type", "application/json.*", "application/json")
                .body(PactDslJsonArray.arrayMinLike(1)
                        .integerType("id", 1)
                        .stringType("name", "Laptop")
                        .numberType("price", 55000.0)
                        .integerType("stock", 10)
                        .closeObject())
                .toPact();
    }

    @Pact(consumer = "product-frontend")
    public RequestResponsePact getMissingProductPact(PactDslWithProvider builder) {
        return builder
                .given("no product with id 999 exists")
                .uponReceiving("a request for a product that does not exist")
                .path("/api/products/999")
                .method("GET")
                .willRespondWith()
                .status(404)
                .matchHeader("Content-Type", "application/json.*", "application/json")
                .body(new PactDslJsonBody()
                        .integerType("status", 404)
                        .stringType("error", "Not Found")
                        .stringType("message", "Product with id 999 not found"))
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getSingleProductPact")
    @DisplayName("client can read a single product")
    void clientReadsSingleProduct(MockServer mockServer) {
        Product product = new ProductClient(mockServer.getUrl()).getProduct(1);

        assertEquals(1L, product.getId());
        assertEquals("Laptop", product.getName());
        assertEquals(55000.0, product.getPrice());
        assertEquals(10, product.getStock());
    }

    @Test
    @PactTestFor(pactMethod = "getAllProductsPact")
    @DisplayName("client can read the product list")
    void clientReadsProductList(MockServer mockServer) {
        List<Product> products = new ProductClient(mockServer.getUrl()).getAllProducts();

        assertFalse(products.isEmpty());
        assertEquals("Laptop", products.get(0).getName());
    }

    @Test
    @PactTestFor(pactMethod = "getMissingProductPact")
    @DisplayName("client receives a 404 error body for an unknown product")
    void clientHandlesMissingProduct(MockServer mockServer) {
        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.web.client.HttpClientErrorException.NotFound.class,
                () -> new ProductClient(mockServer.getUrl()).getProduct(999));
    }
}
