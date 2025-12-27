package com.fw.irongate.repositories;

import com.fw.irongate.models.entities.Permission;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface PermissionRepository
    extends JpaRepository<Permission, UUID>, JpaSpecificationExecutor<Permission> {

  @Query(
      value =
          "SELECT p FROM Permission p "
              + "JOIN FETCH p.role r "
              + "JOIN FETCH p.resourcePath rp "
              + "JOIN FETCH r.sysconfigType rst "
              + "JOIN FETCH rp.sysconfigType rpst "
              + "WHERE r.id = ?1 AND "
              + "rp.value = ?2 AND "
              + "p.deletedAt IS NULL AND "
              + "r.deletedAt IS NULL AND "
              + "rp.deletedAt IS NULL")
  List<Permission> findAllActiveByRoleIdAndResourcePath(UUID roleId, String resourcePath);
}
