package com.fw.irongate.models.dto;

import java.util.List;

public record PermissionDTO(String roleName, List<String> resourcePaths) {}
