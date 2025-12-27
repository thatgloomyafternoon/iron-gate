package com.fw.irongate.repositories;

import com.fw.irongate.models.dto.ChartDataDTO;
import com.fw.irongate.models.entities.Shipment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ShipmentRepository
    extends JpaRepository<Shipment, UUID>, JpaSpecificationExecutor<Shipment> {

  @Query(
      value =
          "SELECT sh FROM Shipment sh "
              + "JOIN FETCH sh.stock "
              + "JOIN FETCH sh.stock.product "
              + "JOIN FETCH sh.stock.warehouse "
              + "JOIN FETCH sh.destWarehouse "
              + "WHERE sh.id = ?1 AND "
              + "sh.deletedAt IS NULL")
  Optional<Shipment> findByIdWithRelations(UUID id);

  @Query(
      value =
          "SELECT sh FROM Shipment sh "
              + "WHERE sh.status = ?1 AND "
              + "sh.assignedTo = ?2 AND "
              + "sh.deletedAt IS NULL")
  Optional<Shipment> findByStatusAndAssignedTo(String status, String assignedTo);

  @Query(
      "SELECT new com.fw.irongate.models.dto.ChartDataDTO(s.status, COUNT(s)) "
          + "FROM Shipment s "
          + "WHERE s.deletedAt IS NULL "
          + "GROUP BY s.status")
  List<ChartDataDTO> countShipmentsByStatus();
}
