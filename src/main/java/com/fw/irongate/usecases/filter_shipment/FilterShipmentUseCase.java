package com.fw.irongate.usecases.filter_shipment;

import static com.fw.irongate.constants.MessageConstants.NOT_TIED_TO_WAREHOUSE;

import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.models.dto.ShipmentDTO;
import com.fw.irongate.models.entities.Shipment;
import com.fw.irongate.models.entities.Warehouse;
import com.fw.irongate.models.entities.WarehouseUser;
import com.fw.irongate.repositories.ShipmentRepository;
import com.fw.irongate.repositories.WarehouseUserRepository;
import com.fw.irongate.repositories.specs.ShipmentSpecification;
import com.fw.irongate.usecases.UseCase;
import com.fw.irongate.web.responses.PaginatedResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@UseCase
public class FilterShipmentUseCase {

  private final ShipmentRepository shipmentRepository;
  private final WarehouseUserRepository warehouseUserRepository;

  public FilterShipmentUseCase(
      ShipmentRepository shipmentRepository, WarehouseUserRepository warehouseUserRepository) {
    this.shipmentRepository = shipmentRepository;
    this.warehouseUserRepository = warehouseUserRepository;
  }

  public PaginatedResponse<ShipmentDTO> handle(
      JwtClaimDTO jwtClaimDTO, FilterShipmentRequest request) {
    List<UUID> warehouseIds =
        warehouseUserRepository.findAllByUserId(jwtClaimDTO.userId()).stream()
            .map(WarehouseUser::getWarehouse)
            .map(Warehouse::getId)
            .toList();
    if (warehouseIds.isEmpty()) {
      throw new IllegalArgumentException(NOT_TIED_TO_WAREHOUSE);
    }
    Pageable pageable =
        PageRequest.of(request.page(), request.size(), Sort.by("createdAt").descending());
    Page<Shipment> shipmentPage =
        shipmentRepository.findAll(
            ShipmentSpecification.getSpecification(request, warehouseIds), pageable);
    List<ShipmentDTO> shipmentDTOList =
        shipmentPage.getContent().stream()
            .map(
                sh ->
                    new ShipmentDTO(
                        sh.getId(),
                        sh.getStock().getProduct().getName(),
                        sh.getQuantity(),
                        sh.getStock().getWarehouse().getName(),
                        sh.getDestWarehouse().getName(),
                        sh.getStatus(),
                        sh.getCode(),
                        sh.getAssignedTo(),
                        sh.getCreatedAt(),
                        sh.getCreatedBy(),
                        sh.getUpdatedAt(),
                        sh.getUpdatedBy()))
            .toList();
    return new PaginatedResponse<>(
        shipmentDTOList,
        shipmentPage.getNumber(),
        shipmentPage.getTotalElements(),
        shipmentPage.getTotalPages());
  }
}
