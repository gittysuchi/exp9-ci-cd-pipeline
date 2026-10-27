package com.exp9.cicd.api;

import com.exp9.cicd.model.Product;
import com.exp9.cicd.service.ProductNotFoundException;
import com.exp9.cicd.service.ProductService;
import com.exp9.cicd.web.ProductController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** API TESTS (web layer only) - MockMvc with a mocked service. Fast: no database, no server port. */
@WebMvcTest(ProductController.class)
@DisplayName("API tests - ProductController (MockMvc)")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService service;

    private Product laptop() {
        Product p = new Product("Laptop", 55000.0, 10);
        p.setId(1L);
        return p;
    }

    @Test
    @DisplayName("GET /api/products -> 200 and a JSON array")
    void listProducts() throws Exception {
        when(service.findAll()).thenReturn(List.of(laptop()));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Laptop"));
    }

    @Test
    @DisplayName("GET /api/products/1 -> 200 with the product")
    void getProduct() throws Exception {
        when(service.findById(1L)).thenReturn(laptop());

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.price").value(55000.0))
                .andExpect(jsonPath("$.stock").value(10));
    }

    @Test
    @DisplayName("GET /api/products/99 -> 404 with an error body")
    void getUnknownProduct() throws Exception {
        when(service.findById(99L)).thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product with id 99 not found"));
    }

    @Test
    @DisplayName("POST /api/products with valid JSON -> 201 + Location header")
    void createProduct() throws Exception {
        when(service.create(any())).thenReturn(laptop());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Laptop\",\"price\":55000.0,\"stock\":10}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/products/1"))
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    @DisplayName("POST /api/products with invalid JSON -> 400 listing the bad fields")
    void createInvalidProduct() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"price\":-5,\"stock\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("name")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("price")));
    }

    @Test
    @DisplayName("GET /api/products/1/discount?percent=10 -> 200 with final price")
    void discountEndpoint() throws Exception {
        when(service.findById(1L)).thenReturn(laptop());
        when(service.priceWithDiscount(eq(1L), eq(10.0))).thenReturn(49500.0);

        mockMvc.perform(get("/api/products/1/discount").param("percent", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalPrice").value(55000.0))
                .andExpect(jsonPath("$.finalPrice").value(49500.0));
    }

    @Test
    @DisplayName("GET discount with an invalid percent -> 400")
    void discountEndpointRejectsInvalidPercent() throws Exception {
        when(service.findById(1L)).thenReturn(laptop());
        when(service.priceWithDiscount(eq(1L), eq(150.0)))
                .thenThrow(new IllegalArgumentException("discount percent must be between 0 and 100"));

        mockMvc.perform(get("/api/products/1/discount").param("percent", "150"))
                .andExpect(status().isBadRequest());
    }
}
