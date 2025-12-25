package com.fw.irongate.usecases.filter_shipment;

import com.fw.irongate.models.dto.ShipmentDTO;
import com.fw.irongate.models.entities.Shipment;
import com.fw.irongate.repositories.ShipmentRepository;
import com.fw.irongate.usecases.UseCase;
import com.fw.irongate.web.responses.PaginatedResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@UseCase
public class FilterShipmentUseCase {

  private final ShipmentRepository shipmentRepository;

  public FilterShipmentUseCase(ShipmentRepository shipmentRepository) {
    this.shipmentRepository = shipmentRepository;
  }

  public PaginatedResponse<ShipmentDTO> handle(FilterShipmentRequest request) {
    Pageable pageable =
        PageRequest.of(request.page(), request.size(), Sort.by("createdAt").descending());
    Page<Shipment> shipmentPage = shipmentRepository.findAllWithRelations(pageable);
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
