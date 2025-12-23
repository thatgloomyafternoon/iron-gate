package com.fw.irongate.models.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

public record WarehouseDTO(
    UUID id, String name, String code, ZonedDateTime createdAt, String createdBy) {}
