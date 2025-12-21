package com.fw.irongate.models.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductDTO(UUID id, String name, String sku, String description, BigDecimal price) {}
