package com.fw.irongate.models.dto;

import java.util.UUID;

/**
 * For now this is only used for <code>/api/auth/me</code>
 * @param id
 * @param email
 * @param roleName
 * @param fullName
 */
public record UserDTO(UUID id, String email, String roleName, String fullName) {}
