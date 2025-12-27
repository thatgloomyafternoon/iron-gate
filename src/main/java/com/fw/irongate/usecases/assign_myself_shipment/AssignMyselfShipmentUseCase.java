package com.fw.irongate.usecases.assign_myself_shipment;

import static com.fw.irongate.constants.MessageConstants.INVALID_STATE;
import static com.fw.irongate.constants.MessageConstants.OPERATION_NOT_PERMITTED;
import static com.fw.irongate.constants.MessageConstants.SHIPMENT_NOT_FOUND;
import static com.fw.irongate.constants.SystemConstants.EVENT_SHIPMENT_UPDATED;
import static com.fw.irongate.constants.SystemConstants.OK;

import com.fw.irongate.models.dto.DashboardEventDTO;
import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.models.entities.Shipment;
import com.fw.irongate.models.entities.WarehouseUser;
import com.fw.irongate.models.enums.ShipmentStatus;
import com.fw.irongate.repositories.ShipmentRepository;
import com.fw.irongate.repositories.WarehouseUserRepository;
import com.fw.irongate.usecases.UseCase;
import com.fw.irongate.usecases.stream_dashboard.StreamDashboardUseCase;
import com.fw.irongate.web.responses.MessageResponse;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.scheduling.TaskScheduler;

@UseCase
public class AssignMyselfShipmentUseCase {

  private final ShipmentRepository shipmentRepository;
  private final WarehouseUserRepository warehouseUserRepository;
  private final TaskScheduler taskScheduler;
  private final StreamDashboardUseCase streamDashboardUseCase;

  public AssignMyselfShipmentUseCase(
      ShipmentRepository shipmentRepository,
      WarehouseUserRepository warehouseUserRepository,
      TaskScheduler taskScheduler,
      StreamDashboardUseCase streamDashboardUseCase) {
    this.shipmentRepository = shipmentRepository;
    this.warehouseUserRepository = warehouseUserRepository;
    this.taskScheduler = taskScheduler;
    this.streamDashboardUseCase = streamDashboardUseCase;
  }

  public MessageResponse handle(JwtClaimDTO jwtClaimDTO, UUID shipmentId) {
    /* check if the shipment exists */
    Optional<Shipment> optShipment = shipmentRepository.findByIdWithRelations(shipmentId);
    if (optShipment.isEmpty()) {
      throw new IllegalArgumentException(SHIPMENT_NOT_FOUND);
    }
    Shipment shipment = optShipment.get();
    /* check if the shipment is in PENDING state */
    if (!shipment.getStatus().equals(ShipmentStatus.PENDING.name())) {
      throw new IllegalArgumentException(INVALID_STATE);
    }
    /* check if the user picking up the shipment (a.k.a the driver) */
    /* is a driver belongs to the origin warehouse */
    List<WarehouseUser> warehouseUsers =
        warehouseUserRepository.findByWarehouseIdAndUserId(
            shipment.getStock().getWarehouse().getId(), jwtClaimDTO.userId());
    if (warehouseUsers.isEmpty()) {
      throw new IllegalArgumentException(OPERATION_NOT_PERMITTED);
    }
    /* check if the driver is currently assigned to */
    /* another ongoing shipment */
    Optional<Shipment> optActiveShipment =
        shipmentRepository.findByStatusAndAssignedTo(
            ShipmentStatus.IN_DELIVERY.name(), jwtClaimDTO.email());
    if (optActiveShipment.isPresent()) {
      throw new IllegalArgumentException(OPERATION_NOT_PERMITTED);
    }
    /* process the shipment */
    shipment.setUpdatedBy(jwtClaimDTO.email());
    shipment.setAssignedTo(jwtClaimDTO.email());
    shipment.setStatus(ShipmentStatus.IN_DELIVERY.name());
    shipmentRepository.save(shipment);
    taskScheduler.schedule(
        () -> progressShipmentToNextState(shipment.getId()), Instant.now().plusSeconds(10));
    return new MessageResponse(OK);
  }

  public void progressShipmentToNextState(UUID shipmentId) {
    Optional<Shipment> optShipment = shipmentRepository.findById(shipmentId);
    if (optShipment.isEmpty()) {
      throw new IllegalArgumentException(OPERATION_NOT_PERMITTED);
    }
    Shipment shipment = optShipment.get();
    shipment.setStatus(ShipmentStatus.ALMOST_THERE.name());
    shipmentRepository.save(shipment);
    streamDashboardUseCase.broadcast(new DashboardEventDTO(EVENT_SHIPMENT_UPDATED));
  }
}
