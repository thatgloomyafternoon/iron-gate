package com.fw.irongate.usecases.get_stock_details;

import static com.fw.irongate.constants.MessageConstants.OPERATION_NOT_PERMITTED;
import static com.fw.irongate.constants.MessageConstants.STOCK_NOT_FOUND;

import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.models.dto.StockDTO;
import com.fw.irongate.models.entities.Stock;
import com.fw.irongate.models.entities.WarehouseUser;
import com.fw.irongate.repositories.StockRepository;
import com.fw.irongate.repositories.WarehouseUserRepository;
import com.fw.irongate.usecases.UseCase;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@UseCase
public class GetStockDetailsUseCase {

  private final StockRepository stockRepository;
  private final WarehouseUserRepository warehouseUserRepository;

  public GetStockDetailsUseCase(
      StockRepository stockRepository, WarehouseUserRepository warehouseUserRepository) {
    this.stockRepository = stockRepository;
    this.warehouseUserRepository = warehouseUserRepository;
  }

  public StockDTO handle(JwtClaimDTO jwtClaimDTO, UUID id) {
    /* check if stock exists */
    Optional<Stock> optStock = stockRepository.findByIdWithRelations(id);
    if (optStock.isEmpty()) {
      throw new IllegalArgumentException(STOCK_NOT_FOUND);
    }
    /* check if user has mapping to the warehouse related to the stock */
    List<WarehouseUser> warehouseUsers =
        warehouseUserRepository.findByWarehouseIdAndUserId(
            optStock.get().getWarehouse().getId(), jwtClaimDTO.userId());
    if (warehouseUsers.isEmpty()) {
      throw new IllegalArgumentException(OPERATION_NOT_PERMITTED);
    }
    /* actual fetch */
    Stock stock = optStock.get();
    return new StockDTO(
        stock.getId(),
        stock.getProduct().getName(),
        stock.getWarehouse().getName(),
        stock.getQuantity(),
        stock.getAllocated(),
        stock.getCreatedAt(),
        stock.getCreatedBy(),
        stock.getUpdatedAt(),
        stock.getUpdatedBy());
  }
}
