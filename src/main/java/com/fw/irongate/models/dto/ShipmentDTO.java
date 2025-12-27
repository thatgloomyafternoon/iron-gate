package com.fw.irongate.models.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

public record ShipmentDTO(
    UUID id,
    String productName,
    Integer quantity,
    String fromWarehouse,
    String toWarehouse,
    String status,
    String code,
    String assignedTo,
    ZonedDateTime createdAt,
    String createdBy,
    ZonedDateTime updatedAt,
    String updatedBy) {}
