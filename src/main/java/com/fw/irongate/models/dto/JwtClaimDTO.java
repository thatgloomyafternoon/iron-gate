package com.fw.irongate.models.dto;

import java.util.UUID;

public record JwtClaimDTO(
    UUID userId, String email, UUID roleId, String roleName, String fullName) {}
