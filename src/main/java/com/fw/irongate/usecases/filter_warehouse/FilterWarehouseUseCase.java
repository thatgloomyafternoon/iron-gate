package com.fw.irongate.usecases.filter_warehouse;

import com.fw.irongate.models.dto.WarehouseDTO;
import com.fw.irongate.models.entities.Warehouse;
import com.fw.irongate.repositories.WarehouseRepository;
import com.fw.irongate.repositories.specs.WarehouseSpecification;
import com.fw.irongate.usecases.UseCase;
import com.fw.irongate.web.responses.PaginatedResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@UseCase
public class FilterWarehouseUseCase {

  private final WarehouseRepository warehouseRepository;

  public FilterWarehouseUseCase(WarehouseRepository warehouseRepository) {
    this.warehouseRepository = warehouseRepository;
  }

  public PaginatedResponse<WarehouseDTO> handle(FilterWarehouseRequest request) {
    Pageable pageable =
        PageRequest.of(request.page(), request.size(), Sort.by("createdAt").descending());
    Page<Warehouse> page =
        warehouseRepository.findAll(WarehouseSpecification.getSpecification(request), pageable);
    List<WarehouseDTO> warehouseDTOList =
        page.getContent().stream()
            .map(
                w ->
                    new WarehouseDTO(
                        w.getId(), w.getName(), w.getCode(), w.getCreatedAt(), w.getCreatedBy()))
            .toList();
    return new PaginatedResponse<>(
        warehouseDTOList, page.getNumber(), page.getTotalElements(), page.getTotalPages());
  }
}
