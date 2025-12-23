package com.fw.irongate.usecases.filter_stock;

import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.models.dto.StockDTO;
import com.fw.irongate.models.entities.Stock;
import com.fw.irongate.models.entities.Warehouse;
import com.fw.irongate.models.entities.WarehouseUser;
import com.fw.irongate.repositories.StockRepository;
import com.fw.irongate.repositories.WarehouseUserRepository;
import com.fw.irongate.repositories.specs.StockSpecification;
import com.fw.irongate.usecases.UseCase;
import com.fw.irongate.web.responses.PaginatedResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@UseCase
public class FilterStockUseCase {

  private final StockRepository stockRepository;
  private final WarehouseUserRepository warehouseUserRepository;

  public FilterStockUseCase(
      StockRepository stockRepository, WarehouseUserRepository warehouseUserRepository) {
    this.stockRepository = stockRepository;
    this.warehouseUserRepository = warehouseUserRepository;
  }

  public PaginatedResponse<StockDTO> handle(JwtClaimDTO jwtClaimDTO, FilterStockRequest request) {
    List<UUID> warehouseIds =
        warehouseUserRepository.findAllByUserId(jwtClaimDTO.userId()).stream()
            .map(WarehouseUser::getWarehouse)
            .map(Warehouse::getId)
            .toList();
    Pageable pageable =
        PageRequest.of(request.page(), request.size(), Sort.by("createdAt").descending());
    Page<Stock> stockPage =
        stockRepository.findAll(
            StockSpecification.getSpecification(request, warehouseIds), pageable);
    List<StockDTO> stockDTOList =
        stockPage.getContent().stream()
            .map(
                s ->
                    new StockDTO(
                        s.getId(),
                        s.getProduct().getName(),
                        s.getWarehouse().getName(),
                        s.getQuantity(),
                        s.getCreatedAt(),
                        s.getCreatedBy(),
                        s.getUpdatedAt(),
                        s.getUpdatedBy()))
            .toList();
    return new PaginatedResponse<>(
        stockDTOList,
        stockPage.getNumber(),
        stockPage.getTotalElements(),
        stockPage.getTotalPages());
  }
}
