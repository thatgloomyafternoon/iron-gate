package com.fw.irongate.models.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public record ProductDTO(
    UUID id,
    String name,
    ZonedDateTime createdAt,
    String createdBy,
    String sku,
    String description,
    BigDecimal price) {}
