package com.fw.irongate.repositories;

import com.fw.irongate.models.entities.WarehouseUser;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WarehouseUserRepository extends JpaRepository<WarehouseUser, UUID> {

  List<WarehouseUser> findAllByUserId(UUID userId);

  @Query(
      value =
          "SELECT wu FROM WarehouseUser wu "
              + "JOIN FETCH Warehouse w ON wu.warehouse.id = w.id "
              + "JOIN FETCH User u ON wu.user.id = u.id "
              + "WHERE w.id = ?1 AND "
              + "u.id = ?2 AND "
              + "wu.deletedAt IS NULL AND "
              + "w.deletedAt IS NULL AND "
              + "u.deletedAt IS NULL")
  List<WarehouseUser> findByWarehouseIdAndUserId(UUID warehouseId, UUID userId);
}
