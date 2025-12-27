package com.fw.irongate.repositories;

import com.fw.irongate.models.dto.ChartDataDTO;
import com.fw.irongate.models.entities.OrderProduct;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderProductRepository extends JpaRepository<OrderProduct, UUID> {

  @Query(
      "SELECT new com.fw.irongate.models.dto.ChartDataDTO(op.product.name, SUM(op.quantity)) "
          + "FROM OrderProduct op "
          + "JOIN op.order o "
          + "WHERE o.status = 'COMPLETED' AND o.deletedAt IS NULL "
          + "GROUP BY op.product.name "
          + "ORDER BY SUM(op.quantity) DESC")
  List<ChartDataDTO> findTopSellingProducts(Pageable pageable);
}
