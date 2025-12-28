package com.fw.irongate.usecases.get_all_permissions;

import com.fw.irongate.models.dto.PermissionDTO;
import com.fw.irongate.models.entities.Permission;
import com.fw.irongate.repositories.PermissionRepository;
import com.fw.irongate.usecases.UseCase;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@UseCase
public class GetAllPermissionsUseCase {

  private final PermissionRepository permissionRepository;

  public GetAllPermissionsUseCase(PermissionRepository permissionRepository) {
    this.permissionRepository = permissionRepository;
  }

  public List<PermissionDTO> handle() {
    List<Permission> permissions = permissionRepository.findAllActive();

    return permissions.stream()
        .collect(
            Collectors.groupingBy(
                p -> p.getRole().getValue(),
                Collectors.mapping(p -> p.getResourcePath().getValue(), Collectors.toList())))
        .entrySet()
        .stream()
        .map(
            entry -> new PermissionDTO(entry.getKey(), entry.getValue().stream().sorted().toList()))
        .sorted(Comparator.comparing(PermissionDTO::roleName))
        .toList();
  }
}
