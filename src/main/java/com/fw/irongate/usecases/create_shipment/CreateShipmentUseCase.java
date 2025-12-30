package com.fw.irongate.usecases.create_shipment;

import static com.fw.irongate.constants.MessageConstants.DEST_WAREHOUSE_NOT_FOUND;
import static com.fw.irongate.constants.MessageConstants.INSUFFICIENT_STOCK;
import static com.fw.irongate.constants.MessageConstants.OPERATION_NOT_PERMITTED;
import static com.fw.irongate.constants.MessageConstants.STOCK_NOT_FOUND;
import static com.fw.irongate.constants.SystemConstants.EVENT_SHIPMENT_CREATED;

import com.fw.irongate.models.dto.DashboardEventDTO;
import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.models.entities.Shipment;
import com.fw.irongate.models.entities.Stock;
import com.fw.irongate.models.entities.Warehouse;
import com.fw.irongate.models.entities.WarehouseUser;
import com.fw.irongate.models.enums.ShipmentStatus;
import com.fw.irongate.repositories.CounterRepository;
import com.fw.irongate.repositories.ShipmentRepository;
import com.fw.irongate.repositories.StockRepository;
import com.fw.irongate.repositories.WarehouseRepository;
import com.fw.irongate.repositories.WarehouseUserRepository;
import com.fw.irongate.usecases.UseCase;
import com.fw.irongate.usecases.stream_dashboard.StreamDashboardUseCase;
import com.fw.irongate.web.responses.IdResponse;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@UseCase
public class CreateShipmentUseCase {

  private final StockRepository stockRepository;
  private final WarehouseUserRepository warehouseUserRepository;
  private final WarehouseRepository warehouseRepository;
  private final ShipmentRepository shipmentRepository;
  private final CounterRepository counterRepository;
  private final StreamDashboardUseCase streamDashboardUseCase;

  public CreateShipmentUseCase(
      StockRepository stockRepository,
      WarehouseUserRepository warehouseUserRepository,
      WarehouseRepository warehouseRepository,
      ShipmentRepository shipmentRepository,
      CounterRepository counterRepository,
      StreamDashboardUseCase streamDashboardUseCase) {
    this.stockRepository = stockRepository;
    this.warehouseUserRepository = warehouseUserRepository;
    this.warehouseRepository = warehouseRepository;
    this.shipmentRepository = shipmentRepository;
    this.counterRepository = counterRepository;
    this.streamDashboardUseCase = streamDashboardUseCase;
  }

  @Transactional
  public IdResponse handle(JwtClaimDTO jwtClaimDTO, CreateShipmentRequest request) {
    /* check if stock record exists */
    Optional<Stock> optStock = stockRepository.findByIdWithRelations(request.stockId());
    if (optStock.isEmpty()) {
      throw new IllegalArgumentException(STOCK_NOT_FOUND);
    }
    Stock stock = optStock.get();
    /* check if there is warehouses_users mapping */
    /* between the warehouse related to the given stock (source warehouse) */
    /* and the user requesting the shipment */
    List<WarehouseUser> warehouseUsers =
        warehouseUserRepository.findByWarehouseIdAndUserId(
            stock.getWarehouse().getId(), jwtClaimDTO.userId());
    if (warehouseUsers.isEmpty()) {
      throw new IllegalArgumentException(OPERATION_NOT_PERMITTED);
    }
    /* check if (quantity - allocated) in the stock satisfies */
    /* the quantity requested */
    if (request.quantity().intValue() > (stock.getQuantity() - stock.getAllocated())) {
      throw new IllegalArgumentException(INSUFFICIENT_STOCK);
    }
    /* check if destination warehouse exists */
    Optional<Warehouse> optDestWarehouse = warehouseRepository.findById(request.destWarehouseId());
    if (optDestWarehouse.isEmpty()) {
      throw new IllegalArgumentException(DEST_WAREHOUSE_NOT_FOUND);
    }
    Warehouse destWarehouse = optDestWarehouse.get();
    /* check if origin warehouse id == dest warehouse id */
    if (stock.getWarehouse().getId().toString().equals(destWarehouse.getId().toString())) {
      throw new IllegalArgumentException(OPERATION_NOT_PERMITTED);
    }
    /* create shipment */
    Shipment shipment = new Shipment();
    shipment.setCreatedBy(jwtClaimDTO.email());
    shipment.setUpdatedBy(jwtClaimDTO.email());
    shipment.setStock(stock);
    shipment.setDestWarehouse(optDestWarehouse.get());
    shipment.setQuantity(request.quantity().intValue());
    shipment.setStatus(ShipmentStatus.PENDING.name());
    shipment.setCode(generateShipmentCode(stock.getWarehouse(), optDestWarehouse.get()));
    shipment = shipmentRepository.save(shipment);
    /* send event to frontend */
    streamDashboardUseCase.broadcast(new DashboardEventDTO(EVENT_SHIPMENT_CREATED));
    /* increment stock.allocated */
    stock.setAllocated(stock.getAllocated() + shipment.getQuantity());
    stockRepository.save(stock);
    return new IdResponse(shipment.getId());
  }

  private String generateShipmentCode(Warehouse fromWarehouse, Warehouse toWarehouse) {
    int count = counterRepository.getNext();
    counterRepository.increment();
    ZonedDateTime now = ZonedDateTime.now();
    return String.format(
        "%s-%s-%s-%s-%s-%s",
        fromWarehouse.getCode(),
        toWarehouse.getCode(),
        now.getYear(),
        now.getMonthValue(),
        now.getDayOfMonth(),
        count);
  }
}
