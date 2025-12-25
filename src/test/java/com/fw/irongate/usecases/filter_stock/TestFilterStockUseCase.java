package com.fw.irongate.usecases.filter_stock;

import static com.fw.irongate.constants.MessageConstants.NOT_TIED_TO_WAREHOUSE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.models.dto.StockDTO;
import com.fw.irongate.models.entities.Product;
import com.fw.irongate.models.entities.Stock;
import com.fw.irongate.models.entities.User;
import com.fw.irongate.models.entities.Warehouse;
import com.fw.irongate.models.entities.WarehouseUser;
import com.fw.irongate.repositories.StockRepository;
import com.fw.irongate.repositories.WarehouseUserRepository;
import com.fw.irongate.web.responses.PaginatedResponse;
import com.github.f4b6a3.uuid.UuidCreator;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class TestFilterStockUseCase {

  @Mock private StockRepository stockRepository;
  @Mock private WarehouseUserRepository warehouseUserRepository;

  @InjectMocks private FilterStockUseCase filterStockUseCase;

  private Stock createStock(Product product, Warehouse warehouse) {
    Stock stock = new Stock();
    stock.setId(UuidCreator.getTimeOrderedEpoch());
    stock.setCreatedAt(ZonedDateTime.now());
    stock.setCreatedBy("system@mail.com");
    stock.setProduct(product);
    stock.setWarehouse(warehouse);
    stock.setQuantity(100);
    return stock;
  }

  private Product createProduct() {
    Product product = new Product();
    product.setId(UuidCreator.getTimeOrderedEpoch());
    product.setName("Test Product");
    product.setSku("SKU-001");
    product.setPrice(new BigDecimal("99.99"));
    return product;
  }

  private Warehouse createWarehouse() {
    Warehouse warehouse = new Warehouse();
    warehouse.setId(UuidCreator.getTimeOrderedEpoch());
    warehouse.setName("New York");
    warehouse.setCode("NYC");
    return warehouse;
  }

  private User createUser(UUID id) {
    User user = new User();
    user.setId(id);
    user.setEmail("system@mail.com");
    return user;
  }

  private WarehouseUser createWarehouseUser(UUID userId, Warehouse warehouse) {
    WarehouseUser warehouseUser = new WarehouseUser();
    warehouseUser.setId(UuidCreator.getTimeOrderedEpoch());
    warehouseUser.setUser(createUser(userId));
    warehouseUser.setWarehouse(warehouse);
    return warehouseUser;
  }

  @SuppressWarnings("unchecked")
  @Test
  void handle_ShouldReturnMappedData_WhenStocksExist() {
    /* 1. Arrange */
    UUID userId = UuidCreator.getTimeOrderedEpoch();
    JwtClaimDTO jwtClaimDTO =
        new JwtClaimDTO(
            userId,
            "user@mail.com",
            UuidCreator.getTimeOrderedEpoch(),
            "ROLE_MANAGER",
            "full name");
    FilterStockRequest request = new FilterStockRequest("test", 500, 0, 10);
    Warehouse warehouse = createWarehouse();
    Product product = createProduct();
    Stock stock = createStock(product, warehouse);
    WarehouseUser warehouseUser = createWarehouseUser(userId, warehouse);
    when(warehouseUserRepository.findAllByUserId(userId)).thenReturn(List.of(warehouseUser));
    Page<Stock> stockPage = new PageImpl<>(List.of(stock));
    when(stockRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(stockPage);
    /* 2. Act */
    PaginatedResponse<StockDTO> response = filterStockUseCase.handle(jwtClaimDTO, request);
    /* 3. Assert */
    assertNotNull(response);
    assertEquals(1, response.totalItems());
    assertEquals(1, response.totalPages());
    assertEquals(0, response.currentPage());
    StockDTO dto = response.data().getFirst();
    assertEquals(stock.getId(), dto.id());
    assertEquals(stock.getQuantity(), dto.quantity());
    assertEquals(product.getName(), dto.productName());
    assertEquals(warehouse.getName(), dto.warehouse());
  }

  @SuppressWarnings("unchecked")
  @Test
  void handle_ShouldReturnEmptyList_WhenNoStocksFound() {
    /* 1. Arrange */
    UUID userId = UuidCreator.getTimeOrderedEpoch();
    JwtClaimDTO jwtClaimDTO =
        new JwtClaimDTO(
            userId,
            "user@mail.com",
            UuidCreator.getTimeOrderedEpoch(),
            "ROLE_MANAGER",
            "full name");
    FilterStockRequest request = new FilterStockRequest("nonexistent", null, 0, 10);
    Warehouse warehouse = createWarehouse();
    WarehouseUser warehouseUser = createWarehouseUser(userId, warehouse);
    when(warehouseUserRepository.findAllByUserId(userId)).thenReturn(List.of(warehouseUser));
    when(stockRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(Page.empty());
    /* 2. Act */
    PaginatedResponse<StockDTO> response = filterStockUseCase.handle(jwtClaimDTO, request);
    /* 3. Assert */
    assertNotNull(response);
    assertTrue(response.data().isEmpty());
    assertEquals(0, response.totalItems());
  }

  @SuppressWarnings("unchecked")
  @Test
  void handle_ShouldApplyCorrectPaginationAndSorting() {
    /* 1. Arrange */
    UUID userId = UuidCreator.getTimeOrderedEpoch();
    JwtClaimDTO jwtClaimDTO =
        new JwtClaimDTO(
            userId,
            "user@mail.com",
            UuidCreator.getTimeOrderedEpoch(),
            "ROLE_MANAGER",
            "full name");
    FilterStockRequest request = new FilterStockRequest(null, null, 2, 5);
    Warehouse warehouse = createWarehouse();
    WarehouseUser warehouseUser = createWarehouseUser(userId, warehouse);
    when(warehouseUserRepository.findAllByUserId(userId)).thenReturn(List.of(warehouseUser));
    when(stockRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(Page.empty());
    /* 2. Act */
    filterStockUseCase.handle(jwtClaimDTO, request);
    /* 3. Assert */
    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    verify(stockRepository).findAll(any(Specification.class), pageableCaptor.capture());
    Pageable capturedPageable = pageableCaptor.getValue();
    assertEquals(2, capturedPageable.getPageNumber());
    assertEquals(5, capturedPageable.getPageSize());
    Sort sort = capturedPageable.getSort();
    assertNotNull(sort.getOrderFor("createdAt"));
    assertTrue(Objects.requireNonNull(sort.getOrderFor("createdAt")).isDescending());
  }

  @Test
  void handle_ShouldWorkWithEmptyWarehouseUserList() {
    /* 1. Arrange */
    UUID userId = UuidCreator.getTimeOrderedEpoch();
    JwtClaimDTO jwtClaimDTO =
        new JwtClaimDTO(
            userId,
            "user@mail.com",
            UuidCreator.getTimeOrderedEpoch(),
            "ROLE_MANAGER",
            "full name");
    FilterStockRequest request = new FilterStockRequest(null, null, 0, 10);
    when(warehouseUserRepository.findAllByUserId(userId)).thenReturn(Collections.emptyList());

    /* 2. Act & 3. Assert */
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> filterStockUseCase.handle(jwtClaimDTO, request));
    assertEquals(NOT_TIED_TO_WAREHOUSE, exception.getMessage());
  }
}
