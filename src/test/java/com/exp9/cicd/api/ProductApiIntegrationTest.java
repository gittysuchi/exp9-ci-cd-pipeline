package com.exp9.cicd.api;

import com.exp9.cicd.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * INTEGRATION TESTS - the whole Spring context (controller -> service -> JPA -> H2 database)
 * is started and exercised through MockMvc. Nothing is mocked.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Integration tests - Product API end to end")
class ProductApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    private void createProduct(String name, double price, int stock) throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"name\":\"%s\",\"price\":%s,\"stock\":%d}", name, price, stock)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("create -> read back -> list works through all layers")
    void createAndReadBack() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Laptop\",\"price\":55000.0,\"stock\":10}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Laptop"));
    }

    @Test
    @DisplayName("discount is calculated from the stored price")
    void discountUsesStoredPrice() throws Exception {
        createProduct("Monitor", 10000.0, 5);
        Long id = repository.findAll().get(0).getId();

        mockMvc.perform(get("/api/products/{id}/discount", id).param("percent", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalPrice").value(10000.0))
                .andExpect(jsonPath("$.finalPrice").value(8000.0));
    }

    @Test
    @DisplayName("low-stock endpoint only returns products under the threshold")
    void lowStockFilter() throws Exception {
        createProduct("Keyboard", 3499.0, 3);
        createProduct("Mouse", 799.0, 25);

        mockMvc.perform(get("/api/products/low-stock").param("threshold", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Keyboard"));
    }

    @Test
    @DisplayName("unknown product -> 404 from the real exception handler")
    void unknownProduct() throws Exception {
        mockMvc.perform(get("/api/products/424242"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("every response carries the release-track headers used by the canary gateway")
    void releaseHeadersArePresent() throws Exception {
        mockMvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-App-Track", "test"))
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
