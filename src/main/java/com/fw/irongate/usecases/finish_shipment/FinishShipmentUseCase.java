package com.fw.irongate.usecases.finish_shipment;

import static com.fw.irongate.constants.MessageConstants.INVALID_STATE;
import static com.fw.irongate.constants.MessageConstants.OPERATION_NOT_PERMITTED;
import static com.fw.irongate.constants.MessageConstants.SHIPMENT_NOT_FOUND;
import static com.fw.irongate.constants.SystemConstants.OK;

import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.models.entities.Shipment;
import com.fw.irongate.models.entities.Stock;
import com.fw.irongate.models.enums.ShipmentStatus;
import com.fw.irongate.repositories.ShipmentRepository;
import com.fw.irongate.repositories.StockRepository;
import com.fw.irongate.usecases.UseCase;
import com.fw.irongate.web.responses.MessageResponse;
import java.util.Optional;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@UseCase
public class FinishShipmentUseCase {

  private final ShipmentRepository shipmentRepository;
  private final StockRepository stockRepository;

  public FinishShipmentUseCase(
      ShipmentRepository shipmentRepository, StockRepository stockRepository) {
    this.shipmentRepository = shipmentRepository;
    this.stockRepository = stockRepository;
  }

  @Transactional
  public MessageResponse handle(JwtClaimDTO jwtClaimDTO, UUID shipmentId) {
    /* check if the shipment exists */
    Optional<Shipment> optShipment = shipmentRepository.findByIdWithRelations(shipmentId);
    if (optShipment.isEmpty()) {
      throw new IllegalArgumentException(SHIPMENT_NOT_FOUND);
    }
    Shipment shipment = optShipment.get();
    /* check if the user calling the api is */
    /* actually the driver assigned for the shipment */
    if (shipment.getAssignedTo() == null || !shipment.getAssignedTo().equals(jwtClaimDTO.email())) {
      throw new IllegalArgumentException(OPERATION_NOT_PERMITTED);
    }
    /* check the status of shipment, it must be ALMOST_THERE */
    /* in order to be finished */
    if (!shipment.getStatus().equals(ShipmentStatus.ALMOST_THERE.name())) {
      throw new IllegalArgumentException(INVALID_STATE);
    }
    /* finish the shipment */
    Stock stock = shipment.getStock();
    stock.setQuantity(stock.getQuantity() - shipment.getQuantity());
    stock.setAllocated(stock.getAllocated() - shipment.getQuantity());
    stock.setUpdatedBy(jwtClaimDTO.email());
    stockRepository.save(stock);
    shipment.setStatus(ShipmentStatus.DELIVERED.name());
    shipment.setUpdatedBy(jwtClaimDTO.email());
    shipmentRepository.save(shipment);
    updateNewStockInDestWarehouse(jwtClaimDTO, shipment);
    return new MessageResponse(OK);
  }

  private void updateNewStockInDestWarehouse(JwtClaimDTO jwtClaimDTO, Shipment shipment) {
    Optional<Stock> optStock =
        stockRepository.findByWarehouseIdAndProductId(
            shipment.getDestWarehouse().getId(), shipment.getStock().getProduct().getId());
    Stock stock;
    if (optStock.isPresent()) {
      /* if it does, update the existing stock */
      stock = optStock.get();
      stock.setUpdatedBy(jwtClaimDTO.email());
      stock.setQuantity(stock.getQuantity() + shipment.getQuantity());
      stockRepository.save(stock);
    } else {
      /* else create the stock */
      stock = new Stock();
      stock.setCreatedBy(jwtClaimDTO.email());
      stock.setUpdatedBy(jwtClaimDTO.email());
      stock.setProduct(shipment.getStock().getProduct());
      stock.setWarehouse(shipment.getDestWarehouse());
      stock.setQuantity(shipment.getQuantity());
      stockRepository.save(stock);
    }
  }
}
