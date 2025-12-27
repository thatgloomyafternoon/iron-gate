package com.fw.irongate.models.dto;

import java.util.UUID;

/**
 * Used for:<br>
 * - <code>/api/auth/me</code><br>
 * - <code>/api/warehouse/details</code>
 * @param id
 * @param email
 * @param roleName
 * @param fullName
 */
public record UserDTO(UUID id, String email, String roleName, String fullName) {}
