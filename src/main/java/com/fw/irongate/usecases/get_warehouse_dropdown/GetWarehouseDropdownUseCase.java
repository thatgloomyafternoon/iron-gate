package com.fw.irongate.usecases.get_warehouse_dropdown;

import com.fw.irongate.models.dto.WarehouseDropdownDTO;
import com.fw.irongate.repositories.WarehouseRepository;
import com.fw.irongate.usecases.UseCase;
import java.util.List;

@UseCase
public class GetWarehouseDropdownUseCase {

  private final WarehouseRepository warehouseRepository;

  public GetWarehouseDropdownUseCase(WarehouseRepository warehouseRepository) {
    this.warehouseRepository = warehouseRepository;
  }

  public List<WarehouseDropdownDTO> handle() {
    return warehouseRepository.findAll().stream()
        .map(w -> new WarehouseDropdownDTO(w.getId(), w.getName()))
        .toList();
  }
}
