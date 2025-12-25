package com.fw.irongate.repositories;

import com.fw.irongate.models.entities.Stock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface StockRepository
    extends JpaRepository<Stock, UUID>, JpaSpecificationExecutor<Stock> {

  Optional<Stock> findByWarehouseIdAndProductId(UUID warehouseId, UUID productId);

  @Query(
      value = "SELECT s FROM Stock s JOIN FETCH s.warehouse JOIN FETCH s.product WHERE s.id = ?1")
  Optional<Stock> findByIdWithRelations(UUID id);

  @Query(
      value =
          "SELECT s FROM Stock s JOIN FETCH s.warehouse JOIN FETCH s.product WHERE s.warehouse.id = ?1")
  List<Stock> findAllByWarehouseId(UUID warehouseId);
}
