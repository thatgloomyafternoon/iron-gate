package com.fw.irongate.web.api;

import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.models.dto.OrderDTO;
import com.fw.irongate.usecases.filter_order.FilterOrderRequest;
import com.fw.irongate.usecases.filter_order.FilterOrderUseCase;
import com.fw.irongate.usecases.fulfill_order.FulfillOrderUseCase;
import com.fw.irongate.web.responses.MessageResponse;
import com.fw.irongate.web.responses.PaginatedResponse;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order")
public class OrderController {

  private final FilterOrderUseCase filterOrderUseCase;
  private final FulfillOrderUseCase fulfillOrderUseCase;

  public OrderController(
      FilterOrderUseCase filterOrderUseCase, FulfillOrderUseCase fulfillOrderUseCase) {
    this.filterOrderUseCase = filterOrderUseCase;
    this.fulfillOrderUseCase = fulfillOrderUseCase;
  }

  @GetMapping("/filter")
  public ResponseEntity<PaginatedResponse<OrderDTO>> filter(
      @AuthenticationPrincipal JwtClaimDTO jwtClaimDTO,
      @RequestParam(required = false) String customerName,
      @RequestParam(required = false) String toWarehouse,
      @RequestParam(required = false) String productName,
      @RequestParam(required = false) BigDecimal minTotalPrice,
      @RequestParam(required = false) BigDecimal maxTotalPrice,
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(
        filterOrderUseCase.handle(
            jwtClaimDTO,
            new FilterOrderRequest(
                customerName,
                toWarehouse,
                productName,
                minTotalPrice,
                maxTotalPrice,
                status,
                page,
                size)));
  }

  @PatchMapping("/fulfill")
  public ResponseEntity<MessageResponse> fulfill(
      @AuthenticationPrincipal JwtClaimDTO jwtClaimDTO, @RequestParam(name = "id") UUID orderId) {
    return ResponseEntity.ok(fulfillOrderUseCase.handle(jwtClaimDTO, orderId));
  }
}
