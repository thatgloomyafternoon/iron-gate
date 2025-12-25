package com.fw.irongate.web.api;

import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.models.dto.ShipmentDTO;
import com.fw.irongate.usecases.assign_myself_shipment.AssignMyselfShipmentUseCase;
import com.fw.irongate.usecases.create_shipment.CreateShipmentRequest;
import com.fw.irongate.usecases.create_shipment.CreateShipmentUseCase;
import com.fw.irongate.usecases.filter_shipment.FilterShipmentRequest;
import com.fw.irongate.usecases.filter_shipment.FilterShipmentUseCase;
import com.fw.irongate.usecases.finish_shipment.FinishShipmentUseCase;
import com.fw.irongate.web.responses.IdResponse;
import com.fw.irongate.web.responses.MessageResponse;
import com.fw.irongate.web.responses.PaginatedResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shipment")
public class ShipmentController {

  private final CreateShipmentUseCase createShipmentUseCase;
  private final FilterShipmentUseCase filterShipmentUseCase;
  private final AssignMyselfShipmentUseCase assignMyselfShipmentUseCase;
  private final FinishShipmentUseCase finishShipmentUseCase;

  public ShipmentController(
      CreateShipmentUseCase createShipmentUseCase,
      FilterShipmentUseCase filterShipmentUseCase,
      AssignMyselfShipmentUseCase assignMyselfShipmentUseCase,
      FinishShipmentUseCase finishShipmentUseCase) {
    this.createShipmentUseCase = createShipmentUseCase;
    this.filterShipmentUseCase = filterShipmentUseCase;
    this.assignMyselfShipmentUseCase = assignMyselfShipmentUseCase;
    this.finishShipmentUseCase = finishShipmentUseCase;
  }

  @GetMapping("/filter")
  public ResponseEntity<PaginatedResponse<ShipmentDTO>> filter(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(filterShipmentUseCase.handle(new FilterShipmentRequest(page, size)));
  }

  @PostMapping("/create")
  public ResponseEntity<IdResponse> create(
      @AuthenticationPrincipal JwtClaimDTO jwtClaimDTO,
      @RequestBody @Valid CreateShipmentRequest request) {
    return ResponseEntity.ok(createShipmentUseCase.handle(jwtClaimDTO, request));
  }

  @PatchMapping("/assign-myself")
  public ResponseEntity<MessageResponse> assignMyself(
      @AuthenticationPrincipal JwtClaimDTO jwtClaimDTO,
      @RequestParam(name = "id") UUID shipmentId) {
    return ResponseEntity.ok(assignMyselfShipmentUseCase.handle(jwtClaimDTO, shipmentId));
  }

  @PatchMapping("/finish")
  public ResponseEntity<MessageResponse> finish(
      @AuthenticationPrincipal JwtClaimDTO jwtClaimDTO,
      @RequestParam(name = "id") UUID shipmentId) {
    return ResponseEntity.ok(finishShipmentUseCase.handle(jwtClaimDTO, shipmentId));
  }
}
