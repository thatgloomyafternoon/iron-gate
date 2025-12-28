package com.fw.irongate.repositories;

import com.fw.irongate.models.dto.ChartDataDTO;
import com.fw.irongate.models.entities.Order;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository
    extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {

  @Query(
      value =
          "SELECT o FROM Order o JOIN FETCH o.orderProducts JOIN FETCH o.orderProducts.product JOIN FETCH o.warehouse WHERE o.id = ?1")
  Optional<Order> findByIdWithRelations(UUID id);

  @Query(
      "SELECT new com.fw.irongate.models.dto.ChartDataDTO(o.status, COUNT(o)) "
          + "FROM Order o "
          + "WHERE o.deletedAt IS NULL "
          + "GROUP BY o.status")
  List<ChartDataDTO> countOrdersByStatus();

  @Query(
      "SELECT new com.fw.irongate.models.dto.ChartDataDTO(o.warehouse.name, SUM(o.totalPrice)) "
          + "FROM Order o "
          + "WHERE o.status = 'COMPLETED' AND o.deletedAt IS NULL "
          + "GROUP BY o.warehouse.name "
          + "ORDER BY SUM(o.totalPrice) DESC")
  List<ChartDataDTO> findTopRevenueByWarehouse(org.springframework.data.domain.Pageable pageable);
}
