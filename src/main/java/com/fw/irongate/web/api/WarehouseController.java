package com.fw.irongate.web.api;

import com.fw.irongate.models.dto.WarehouseDTO;
import com.fw.irongate.models.dto.WarehouseDetailsDTO;
import com.fw.irongate.models.dto.WarehouseDropdownDTO;
import com.fw.irongate.usecases.filter_warehouse.FilterWarehouseRequest;
import com.fw.irongate.usecases.filter_warehouse.FilterWarehouseUseCase;
import com.fw.irongate.usecases.get_warehouse_details.GetWarehouseDetailsUseCase;
import com.fw.irongate.usecases.get_warehouse_dropdown.GetWarehouseDropdownUseCase;
import com.fw.irongate.web.responses.PaginatedResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/warehouse")
public class WarehouseController {

  private final FilterWarehouseUseCase filterWarehouseUseCase;
  private final GetWarehouseDropdownUseCase getWarehouseDropdownUseCase;
  private final GetWarehouseDetailsUseCase getWarehouseDetailsUseCase;

  public WarehouseController(
      FilterWarehouseUseCase filterWarehouseUseCase,
      GetWarehouseDropdownUseCase getWarehouseDropdownUseCase,
      GetWarehouseDetailsUseCase getWarehouseDetailsUseCase) {
    this.filterWarehouseUseCase = filterWarehouseUseCase;
    this.getWarehouseDropdownUseCase = getWarehouseDropdownUseCase;
    this.getWarehouseDetailsUseCase = getWarehouseDetailsUseCase;
  }

  @GetMapping("/filter")
  public ResponseEntity<PaginatedResponse<WarehouseDTO>> filter(
      @RequestParam(required = false) String query,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    FilterWarehouseRequest request = new FilterWarehouseRequest(query, page, size);
    return ResponseEntity.ok(filterWarehouseUseCase.handle(request));
  }

  @GetMapping("/dropdown")
  public ResponseEntity<List<WarehouseDropdownDTO>> getDropdown() {
    return ResponseEntity.ok(getWarehouseDropdownUseCase.handle());
  }

  @GetMapping("/details")
  public ResponseEntity<WarehouseDetailsDTO> getDetails(
      @RequestParam(name = "id") UUID warehouseId) {
    return ResponseEntity.ok(getWarehouseDetailsUseCase.handle(warehouseId));
  }
}
