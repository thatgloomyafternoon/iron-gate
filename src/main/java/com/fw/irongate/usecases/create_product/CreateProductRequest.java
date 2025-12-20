package com.fw.irongate.usecases.create_product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record CreateProductRequest(
    @NotBlank(message = "Name cannot be blank") String name,
    @NotBlank(message = "SKU cannot be blank") String sku,
    String description,
    @NotNull @Positive(message = "Price must be positive") BigDecimal price,
    @NotNull @PositiveOrZero(message = "Quantity must be positive or zero") Integer quantity) {}
