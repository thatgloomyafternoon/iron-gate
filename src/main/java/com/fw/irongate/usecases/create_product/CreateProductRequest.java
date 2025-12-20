package com.fw.irongate.usecases.create_product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateProductRequest(
    @NotBlank(message = "Name cannot be blank") @Size(max = 40, message = "Name max length 40")
        String name,
    @NotBlank(message = "SKU cannot be blank") @Size(max = 15, message = "SKU max length 15")
        String sku,
    @Size(max = 40, message = "Description max length 40") String description,
    @NotNull @Positive(message = "Price must be positive") BigDecimal price,
    @NotNull @PositiveOrZero(message = "Quantity must be positive or zero") Integer quantity) {}
