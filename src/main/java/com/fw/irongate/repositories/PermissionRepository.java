package com.fw.irongate.repositories;

import com.fw.irongate.models.entities.Permission;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {

  @Query(
      value =
          "SELECT p FROM Permission p "
              + "JOIN FETCH Sysconfig r ON p.role.id = r.id "
              + "JOIN FETCH Sysconfig rp ON p.resourcePath.id = rp.id "
              + "JOIN FETCH SysconfigType rst ON r.sysconfigType.id = rst.id "
              + "JOIN FETCH SysconfigType rpst ON rp.sysconfigType.id = rpst.id "
              + "WHERE r.id = ?1 AND "
              + "rp.value = ?2 AND "
              + "p.deletedAt IS NULL AND "
              + "r.deletedAt IS NULL AND "
              + "rp.deletedAt IS NULL")
  List<Permission> findAllActiveByRoleIdAndResourcePath(UUID roleId, String resourcePath);
}
