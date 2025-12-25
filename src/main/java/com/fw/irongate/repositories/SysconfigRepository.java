package com.fw.irongate.repositories;

import com.fw.irongate.models.entities.Sysconfig;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SysconfigRepository extends JpaRepository<Sysconfig, UUID> {

  @Query(value = "SELECT s FROM Sysconfig s WHERE s.id = ?1 AND s.deletedAt IS NULL")
  Optional<Sysconfig> findOneActiveById(UUID id);

  @Query(value = "SELECT s FROM Sysconfig s WHERE s.key = ?1 AND s.deletedAt IS NULL")
  Optional<Sysconfig> findByKey(String key);
}
