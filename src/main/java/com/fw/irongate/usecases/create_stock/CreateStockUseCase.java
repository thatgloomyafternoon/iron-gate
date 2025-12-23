package com.fw.irongate.usecases.create_stock;

import static com.fw.irongate.constants.MessageConstants.OPERATION_NOT_PERMITTED;
import static com.fw.irongate.constants.MessageConstants.PRODUCT_NOT_FOUND;

import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.models.entities.Product;
import com.fw.irongate.models.entities.Stock;
import com.fw.irongate.models.entities.WarehouseUser;
import com.fw.irongate.repositories.ProductRepository;
import com.fw.irongate.repositories.StockRepository;
import com.fw.irongate.repositories.WarehouseUserRepository;
import com.fw.irongate.usecases.UseCase;
import com.fw.irongate.web.responses.IdResponse;
import java.util.List;
import java.util.Optional;

@UseCase
public class CreateStockUseCase {

  private final WarehouseUserRepository warehouseUserRepository;
  private final ProductRepository productRepository;
  private final StockRepository stockRepository;

  public CreateStockUseCase(
      WarehouseUserRepository warehouseUserRepository,
      ProductRepository productRepository,
      StockRepository stockRepository) {
    this.warehouseUserRepository = warehouseUserRepository;
    this.productRepository = productRepository;
    this.stockRepository = stockRepository;
  }

  public IdResponse handle(JwtClaimDTO jwtClaimDTO, CreateStockRequest request) {
    /* check warehouses_users mapping to indicate that: */
    /* - the warehouse exists; */
    /* - the user exists; */
    /* - the user is allowed to create stock in that warehouse */
    List<WarehouseUser> warehouseUsers =
        warehouseUserRepository.findByWarehouseIdAndUserId(
            request.warehouseId(), jwtClaimDTO.userId());
    if (warehouseUsers.isEmpty()) {
      throw new IllegalArgumentException(OPERATION_NOT_PERMITTED);
    }
    WarehouseUser wu = warehouseUsers.getFirst();
    /* check if the product exists */
    Optional<Product> optProduct = productRepository.findById(request.productId());
    if (optProduct.isEmpty()) {
      throw new IllegalArgumentException(PRODUCT_NOT_FOUND);
    }
    /* check if the requested stock exists */
    Optional<Stock> optStock =
        stockRepository.findByWarehouseIdAndProductId(
            wu.getWarehouse().getId(), optProduct.get().getId());
    Stock stock;
    if (optStock.isPresent()) {
      /* if it does, update the existing stock */
      stock = optStock.get();
      stock.setUpdatedBy(jwtClaimDTO.email());
      stock.setQuantity(request.quantity().intValue());
      stock = stockRepository.save(stock);
    } else {
      /* else create the stock */
      stock = new Stock();
      stock.setCreatedBy(jwtClaimDTO.email());
      stock.setUpdatedBy(jwtClaimDTO.email());
      stock.setProduct(optProduct.get());
      stock.setWarehouse(wu.getWarehouse());
      stock.setQuantity(request.quantity().intValue());
      stock = stockRepository.save(stock);
    }
    return new IdResponse(stock.getId());
  }
}
