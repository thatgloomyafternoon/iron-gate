package com.fw.irongate.usecases.finish_shipment;

import static com.fw.irongate.constants.MessageConstants.INVALID_STATE;
import static com.fw.irongate.constants.MessageConstants.OPERATION_NOT_PERMITTED;
import static com.fw.irongate.constants.MessageConstants.SHIPMENT_NOT_FOUND;
import static com.fw.irongate.constants.SystemConstants.OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.models.entities.Product;
import com.fw.irongate.models.entities.Shipment;
import com.fw.irongate.models.entities.Stock;
import com.fw.irongate.models.entities.Warehouse;
import com.fw.irongate.models.enums.ShipmentStatus;
import com.fw.irongate.repositories.ShipmentRepository;
import com.fw.irongate.repositories.StockRepository;
import com.fw.irongate.web.responses.MessageResponse;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TestFinishShipmentUseCase {

  @Mock private ShipmentRepository shipmentRepository;
  @Mock private StockRepository stockRepository;
  @InjectMocks private FinishShipmentUseCase finishShipmentUseCase;

  private JwtClaimDTO createJwtClaims(String email) {
    return new JwtClaimDTO(
        UUID.randomUUID(), email, UUID.randomUUID(), "ROLE_DRIVER", "Driver User");
  }

  private Shipment createShipment(
      UUID id, String status, String assignedTo, Stock stock, Warehouse destWarehouse) {
    Shipment shipment = new Shipment();
    shipment.setId(id);
    shipment.setStatus(status);
    shipment.setAssignedTo(assignedTo);
    shipment.setStock(stock);
    shipment.setDestWarehouse(destWarehouse);
    shipment.setQuantity(10);
    return shipment;
  }

  @Test
  void handle_ShouldFinishShipment_WhenConditionsMet() {
    /* Arrange */
    UUID shipmentId = UUID.randomUUID();
    String driverEmail = "driver@example.com";
    JwtClaimDTO jwt = createJwtClaims(driverEmail);
    Warehouse sourceWarehouse = new Warehouse();
    sourceWarehouse.setId(UUID.randomUUID());
    Warehouse destWarehouse = new Warehouse();
    destWarehouse.setId(UUID.randomUUID());
    Product product = new Product();
    product.setId(UUID.randomUUID());
    Stock sourceStock = new Stock();
    sourceStock.setId(UUID.randomUUID());
    sourceStock.setQuantity(100);
    sourceStock.setAllocated(20);
    sourceStock.setProduct(product);
    sourceStock.setWarehouse(sourceWarehouse);
    Shipment shipment =
        createShipment(
            shipmentId,
            ShipmentStatus.ALMOST_THERE.name(),
            driverEmail,
            sourceStock,
            destWarehouse);
    when(shipmentRepository.findByIdWithRelations(shipmentId)).thenReturn(Optional.of(shipment));
    when(stockRepository.findByWarehouseIdAndProductId(destWarehouse.getId(), product.getId()))
        .thenReturn(Optional.empty());
    /* Act */
    MessageResponse response = finishShipmentUseCase.handle(jwt, shipmentId);
    /* Assert */
    assertNotNull(response);
    assertEquals(OK, response.message());
    /* Verify Source Stock Update */
    assertEquals(90, sourceStock.getQuantity());
    assertEquals(10, sourceStock.getAllocated());
    verify(stockRepository).save(sourceStock);
    /* Verify Shipment Update */
    assertEquals(ShipmentStatus.DELIVERED.name(), shipment.getStatus());
    verify(shipmentRepository).save(shipment);
    /* Verify Dest Stock Creation/Update */
    /* Since we mocked empty, it should save a new stock */
    verify(stockRepository, times(2)).save(any(Stock.class));
  }

  @Test
  void handle_ShouldFinishShipment_WhenDestStockExists() {
    /* Arrange */
    UUID shipmentId = UUID.randomUUID();
    String driverEmail = "driver@example.com";
    JwtClaimDTO jwt = createJwtClaims(driverEmail);
    Warehouse destWarehouse = new Warehouse();
    destWarehouse.setId(UUID.randomUUID());
    Product product = new Product();
    product.setId(UUID.randomUUID());
    Stock sourceStock = new Stock();
    sourceStock.setId(UUID.randomUUID());
    sourceStock.setQuantity(100);
    sourceStock.setAllocated(20);
    sourceStock.setProduct(product);
    Shipment shipment =
        createShipment(
            shipmentId,
            ShipmentStatus.ALMOST_THERE.name(),
            driverEmail,
            sourceStock,
            destWarehouse);
    Stock destStock = new Stock();
    destStock.setQuantity(50);
    destStock.setProduct(product);
    destStock.setWarehouse(destWarehouse);
    when(shipmentRepository.findByIdWithRelations(shipmentId)).thenReturn(Optional.of(shipment));
    when(stockRepository.findByWarehouseIdAndProductId(destWarehouse.getId(), product.getId()))
        .thenReturn(Optional.of(destStock));
    /* Act */
    finishShipmentUseCase.handle(jwt, shipmentId);
    /* Assert */
    assertEquals(60, destStock.getQuantity());
    verify(stockRepository).save(destStock);
  }

  @Test
  void handle_ShouldThrowException_WhenShipmentNotFound() {
    UUID shipmentId = UUID.randomUUID();
    JwtClaimDTO jwt = createJwtClaims("driver@example.com");
    when(shipmentRepository.findByIdWithRelations(shipmentId)).thenReturn(Optional.empty());
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> finishShipmentUseCase.handle(jwt, shipmentId));
    assertEquals(SHIPMENT_NOT_FOUND, exception.getMessage());
  }

  @Test
  void handle_ShouldThrowException_WhenNotAssignedDriver() {
    UUID shipmentId = UUID.randomUUID();
    String driverEmail = "driver@example.com";
    String otherDriver = "other@example.com";
    JwtClaimDTO jwt = createJwtClaims(otherDriver);
    Shipment shipment =
        createShipment(
            shipmentId,
            ShipmentStatus.ALMOST_THERE.name(),
            driverEmail,
            new Stock(),
            new Warehouse());
    when(shipmentRepository.findByIdWithRelations(shipmentId)).thenReturn(Optional.of(shipment));
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> finishShipmentUseCase.handle(jwt, shipmentId));
    assertEquals(OPERATION_NOT_PERMITTED, exception.getMessage());
  }

  @Test
  void handle_ShouldThrowException_WhenAssignedToIsNull() {
    UUID shipmentId = UUID.randomUUID();
    JwtClaimDTO jwt = createJwtClaims("driver@example.com");
    Shipment shipment =
        createShipment(
            shipmentId, ShipmentStatus.ALMOST_THERE.name(), null, new Stock(), new Warehouse());
    when(shipmentRepository.findByIdWithRelations(shipmentId)).thenReturn(Optional.of(shipment));
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> finishShipmentUseCase.handle(jwt, shipmentId));
    assertEquals(OPERATION_NOT_PERMITTED, exception.getMessage());
  }

  @Test
  void handle_ShouldThrowException_WhenStatusInvalid() {
    UUID shipmentId = UUID.randomUUID();
    String driverEmail = "driver@example.com";
    JwtClaimDTO jwt = createJwtClaims(driverEmail);
    Shipment shipment =
        createShipment(
            shipmentId,
            ShipmentStatus.IN_DELIVERY.name(),
            driverEmail,
            new Stock(),
            new Warehouse());
    when(shipmentRepository.findByIdWithRelations(shipmentId)).thenReturn(Optional.of(shipment));
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> finishShipmentUseCase.handle(jwt, shipmentId));
    assertEquals(INVALID_STATE, exception.getMessage());
  }
}
