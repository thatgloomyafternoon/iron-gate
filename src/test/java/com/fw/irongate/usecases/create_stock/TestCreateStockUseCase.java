package com.fw.irongate.usecases.create_stock;

import static com.fw.irongate.constants.MessageConstants.OPERATION_NOT_PERMITTED;
import static com.fw.irongate.constants.MessageConstants.PRODUCT_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.models.entities.Product;
import com.fw.irongate.models.entities.Stock;
import com.fw.irongate.models.entities.Warehouse;
import com.fw.irongate.models.entities.WarehouseUser;
import com.fw.irongate.repositories.ProductRepository;
import com.fw.irongate.repositories.StockRepository;
import com.fw.irongate.repositories.WarehouseUserRepository;
import com.fw.irongate.usecases.stream_dashboard.StreamDashboardUseCase;
import com.fw.irongate.web.responses.IdResponse;
import com.github.f4b6a3.uuid.UuidCreator;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TestCreateStockUseCase {

  @Mock private WarehouseUserRepository warehouseUserRepository;
  @Mock private ProductRepository productRepository;
  @Mock private StockRepository stockRepository;
  @Mock private StreamDashboardUseCase streamDashboardUseCase;
  @InjectMocks private CreateStockUseCase createStockUseCase;

  private JwtClaimDTO jwtClaimDTO;
  private CreateStockRequest request;
  private UUID warehouseId;
  private UUID productId;
  private UUID userId;

  @BeforeEach
  void setUp() {
    warehouseId = UuidCreator.getTimeOrderedEpoch();
    productId = UuidCreator.getTimeOrderedEpoch();
    userId = UuidCreator.getTimeOrderedEpoch();
    jwtClaimDTO =
        new JwtClaimDTO(userId, "test@example.com", UUID.randomUUID(), "ROLE_TEST", "Test User");
    request = new CreateStockRequest(warehouseId, productId, new BigInteger("100"));
  }

  @Test
  void handle_ShouldCreateNewStock_WhenStockDoesNotExist() {
    /* 1. Arrange */
    Warehouse warehouse = new Warehouse();
    warehouse.setId(warehouseId);
    WarehouseUser wu = new WarehouseUser();
    wu.setWarehouse(warehouse);
    Product product = new Product();
    product.setId(productId);
    when(warehouseUserRepository.findByWarehouseIdAndUserId(warehouseId, userId))
        .thenReturn(List.of(wu));
    when(productRepository.findById(productId)).thenReturn(Optional.of(product));
    when(stockRepository.findByWarehouseIdAndProductId(warehouseId, productId))
        .thenReturn(Optional.empty());
    when(stockRepository.save(any(Stock.class)))
        .thenAnswer(
            invocation -> {
              Stock s = invocation.getArgument(0);
              s.setId(UuidCreator.getTimeOrderedEpoch());
              return s;
            });
    doNothing().when(streamDashboardUseCase).broadcast(any());
    /* 2. Act */
    IdResponse response = createStockUseCase.handle(jwtClaimDTO, request);
    /* 3. Assert */
    assertNotNull(response);
    ArgumentCaptor<Stock> stockCaptor = ArgumentCaptor.forClass(Stock.class);
    verify(stockRepository).save(stockCaptor.capture());
    Stock savedStock = stockCaptor.getValue();
    assertEquals(request.quantity().intValue(), savedStock.getQuantity());
    assertEquals(jwtClaimDTO.email(), savedStock.getCreatedBy());
    assertEquals(jwtClaimDTO.email(), savedStock.getUpdatedBy());
    assertEquals(warehouse, savedStock.getWarehouse());
    assertEquals(product, savedStock.getProduct());
  }

  @Test
  void handle_ShouldUpdateExistingStock_WhenStockExists() {
    /* 1. Arrange */
    Warehouse warehouse = new Warehouse();
    warehouse.setId(warehouseId);
    WarehouseUser wu = new WarehouseUser();
    wu.setWarehouse(warehouse);
    Product product = new Product();
    product.setId(productId);
    Stock existingStock = new Stock();
    existingStock.setId(UuidCreator.getTimeOrderedEpoch());
    existingStock.setQuantity(50);
    existingStock.setProduct(product);
    existingStock.setWarehouse(warehouse);
    when(warehouseUserRepository.findByWarehouseIdAndUserId(warehouseId, userId))
        .thenReturn(List.of(wu));
    when(productRepository.findById(productId)).thenReturn(Optional.of(product));
    when(stockRepository.findByWarehouseIdAndProductId(warehouseId, productId))
        .thenReturn(Optional.of(existingStock));
    when(stockRepository.save(any(Stock.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    doNothing().when(streamDashboardUseCase).broadcast(any());
    /* 2. Act */
    IdResponse response = createStockUseCase.handle(jwtClaimDTO, request);
    /* 3. Assert */
    assertNotNull(response);
    assertEquals(existingStock.getId(), response.id());
    ArgumentCaptor<Stock> stockCaptor = ArgumentCaptor.forClass(Stock.class);
    verify(stockRepository).save(stockCaptor.capture());
    Stock savedStock = stockCaptor.getValue();
    assertEquals(request.quantity().intValue(), savedStock.getQuantity()); /* Updated quantity */
    assertEquals(jwtClaimDTO.email(), savedStock.getUpdatedBy());
  }

  @Test
  void handle_ShouldThrowException_WhenUserNotPermittedForWarehouse() {
    /* 1. Arrange */
    when(warehouseUserRepository.findByWarehouseIdAndUserId(warehouseId, userId))
        .thenReturn(Collections.emptyList());
    /* 2. Act & Assert */
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> createStockUseCase.handle(jwtClaimDTO, request));
    assertEquals(OPERATION_NOT_PERMITTED, exception.getMessage());
    verify(stockRepository, never()).save(any(Stock.class));
  }

  @Test
  void handle_ShouldThrowException_WhenProductDoesNotExist() {
    /* 1. Arrange */
    WarehouseUser wu = new WarehouseUser();
    wu.setWarehouse(new Warehouse());
    when(warehouseUserRepository.findByWarehouseIdAndUserId(warehouseId, userId))
        .thenReturn(List.of(wu));
    when(productRepository.findById(productId)).thenReturn(Optional.empty());
    /* 2. Act & Assert */
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> createStockUseCase.handle(jwtClaimDTO, request));
    assertEquals(PRODUCT_NOT_FOUND, exception.getMessage());
    verify(stockRepository, never()).save(any(Stock.class));
  }
}
