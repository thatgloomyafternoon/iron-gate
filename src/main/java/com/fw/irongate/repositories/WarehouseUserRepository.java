package com.fw.irongate.repositories;

import com.fw.irongate.models.entities.WarehouseUser;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseUserRepository extends JpaRepository<WarehouseUser, UUID> {

  List<WarehouseUser> findAllByUserId(UUID userId);
}
