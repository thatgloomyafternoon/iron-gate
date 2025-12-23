package com.fw.irongate.web.api;

import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.models.dto.StockDTO;
import com.fw.irongate.usecases.create_stock.CreateStockRequest;
import com.fw.irongate.usecases.create_stock.CreateStockUseCase;
import com.fw.irongate.usecases.filter_stock.FilterStockRequest;
import com.fw.irongate.usecases.filter_stock.FilterStockUseCase;
import com.fw.irongate.web.responses.IdResponse;
import com.fw.irongate.web.responses.PaginatedResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock")
public class StockController {

  private final FilterStockUseCase filterStockUseCase;
  private final CreateStockUseCase createStockUseCase;

  private StockController(
      FilterStockUseCase filterStockUseCase, CreateStockUseCase createStockUseCase) {
    this.filterStockUseCase = filterStockUseCase;
    this.createStockUseCase = createStockUseCase;
  }

  @GetMapping("/filter")
  public ResponseEntity<PaginatedResponse<StockDTO>> filter(
      @AuthenticationPrincipal JwtClaimDTO jwtClaimDTO,
      @RequestParam(required = false) String query,
      @RequestParam(required = false) Integer maxQuantity,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    FilterStockRequest request = new FilterStockRequest(query, maxQuantity, page, size);
    return ResponseEntity.ok(filterStockUseCase.handle(jwtClaimDTO, request));
  }

  @PostMapping("/create")
  public ResponseEntity<IdResponse> create(
      @AuthenticationPrincipal JwtClaimDTO jwtClaimDTO,
      @RequestBody @Valid CreateStockRequest request) {
    return ResponseEntity.ok(createStockUseCase.handle(jwtClaimDTO, request));
  }
}
