package com.fw.irongate.models.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

public record StockDTO(
    UUID id,
    String productName,
    String warehouse,
    Integer quantity,
    Integer allocated,
    ZonedDateTime createdAt,
    String createdBy,
    ZonedDateTime updatedAt,
    String updatedBy) {}
