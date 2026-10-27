package com.exp9.cicd.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Payload accepted by POST /api/products. */
public record ProductRequest(
        @NotBlank(message = "name must not be blank") String name,
        @NotNull(message = "price is required") @PositiveOrZero(message = "price must be >= 0") Double price,
        @NotNull(message = "stock is required") @PositiveOrZero(message = "stock must be >= 0") Integer stock) {
}
