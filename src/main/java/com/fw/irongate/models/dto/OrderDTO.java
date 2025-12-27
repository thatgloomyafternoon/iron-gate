package com.fw.irongate.models.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public record OrderDTO(
    UUID id,
    String customerName,
    List<OrderProductDTO> orderProducts,
    BigDecimal totalPrice,
    String warehouse,
    String status,
    ZonedDateTime updatedAt,
    String updatedBy) {}
