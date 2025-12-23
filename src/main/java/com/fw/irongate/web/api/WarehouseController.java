package com.fw.irongate.web.api;

import com.fw.irongate.models.dto.WarehouseDTO;
import com.fw.irongate.usecases.filter_warehouse.FilterWarehouseRequest;
import com.fw.irongate.usecases.filter_warehouse.FilterWarehouseUseCase;
import com.fw.irongate.web.responses.PaginatedResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/warehouse")
public class WarehouseController {

  private final FilterWarehouseUseCase filterWarehouseUseCase;

  public WarehouseController(FilterWarehouseUseCase filterWarehouseUseCase) {
    this.filterWarehouseUseCase = filterWarehouseUseCase;
  }

  @GetMapping("/filter")
  public ResponseEntity<PaginatedResponse<WarehouseDTO>> filter(
      @RequestParam(required = false) String query,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    FilterWarehouseRequest request = new FilterWarehouseRequest(query, page, size);
    return ResponseEntity.ok(filterWarehouseUseCase.handle(request));
  }
}
