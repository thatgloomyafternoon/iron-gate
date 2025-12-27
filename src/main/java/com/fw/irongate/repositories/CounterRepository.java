package com.fw.irongate.repositories;

import com.fw.irongate.models.entities.Counter;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CounterRepository extends JpaRepository<Counter, UUID> {

  @Query(value = "SELECT c.next + 1 FROM Counter c WHERE c.deletedAt IS NULL")
  Integer getNext();

  @Modifying
  @Query(value = "UPDATE Counter c SET c.next = c.next + 1 WHERE c.deletedAt IS NULL")
  void increment();
}
