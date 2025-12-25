package com.fw.irongate.usecases.filter_order;

import static com.fw.irongate.constants.MessageConstants.NOT_TIED_TO_WAREHOUSE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.models.dto.OrderDTO;
import com.fw.irongate.models.entities.Order;
import com.fw.irongate.models.entities.OrderProduct;
import com.fw.irongate.models.entities.Product;
import com.fw.irongate.models.entities.User;
import com.fw.irongate.models.entities.Warehouse;
import com.fw.irongate.models.entities.WarehouseUser;
import com.fw.irongate.repositories.OrderRepository;
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
class TestFilterOrderUseCase {

  @Mock private OrderRepository orderRepository;
  @Mock private WarehouseUserRepository warehouseUserRepository;

  @InjectMocks private FilterOrderUseCase filterOrderUseCase;

  private Order createOrder(Warehouse warehouse) {
    Order order = new Order();
    order.setId(UuidCreator.getTimeOrderedEpoch());
    order.setCreatedAt(ZonedDateTime.now());
    order.setCreatedBy("system@mail.com");
    order.setUpdatedAt(ZonedDateTime.now());
    order.setUpdatedBy("system@mail.com");
    order.setCustomerName("Test Customer");
    order.setStatus("CREATED");
    order.setWarehouse(warehouse);
    Product product = new Product();
    product.setName("Test Product");
    OrderProduct op = new OrderProduct();
    op.setProduct(product);
    op.setQuantity(5);
    op.setPrice(new BigDecimal("10.00"));
    order.setOrderProducts(List.of(op));
    return order;
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
  void handle_ShouldReturnMappedData_WhenOrdersExist() {
    /* 1. Arrange */
    UUID userId = UuidCreator.getTimeOrderedEpoch();
    JwtClaimDTO jwtClaimDTO =
        new JwtClaimDTO(
            userId,
            "user@mail.com",
            UuidCreator.getTimeOrderedEpoch(),
            "ROLE_MANAGER",
            "full name");
    FilterOrderRequest request = new FilterOrderRequest("test", 0, 10);
    Warehouse warehouse = createWarehouse();
    Order order = createOrder(warehouse);
    WarehouseUser warehouseUser = createWarehouseUser(userId, warehouse);
    when(warehouseUserRepository.findAllByUserId(userId)).thenReturn(List.of(warehouseUser));
    Page<Order> orderPage = new PageImpl<>(List.of(order));
    when(orderRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(orderPage);
    /* 2. Act */
    PaginatedResponse<OrderDTO> response = filterOrderUseCase.handle(jwtClaimDTO, request);
    /* 3. Assert */
    assertNotNull(response);
    assertEquals(1, response.totalItems());
    assertEquals(1, response.totalPages());
    assertEquals(0, response.currentPage());
    OrderDTO dto = response.data().getFirst();
    assertEquals(order.getId(), dto.id());
    assertEquals(order.getCustomerName(), dto.customerName());
    assertEquals(warehouse.getName(), dto.warehouse());
    assertEquals(new BigDecimal("50.00"), dto.totalPrice()); // 5 * 10.00
  }

  @SuppressWarnings("unchecked")
  @Test
  void handle_ShouldReturnEmptyList_WhenNoOrdersFound() {
    /* 1. Arrange */
    UUID userId = UuidCreator.getTimeOrderedEpoch();
    JwtClaimDTO jwtClaimDTO =
        new JwtClaimDTO(
            userId,
            "user@mail.com",
            UuidCreator.getTimeOrderedEpoch(),
            "ROLE_MANAGER",
            "full name");
    FilterOrderRequest request = new FilterOrderRequest("nonexistent", 0, 10);
    Warehouse warehouse = createWarehouse();
    WarehouseUser warehouseUser = createWarehouseUser(userId, warehouse);
    when(warehouseUserRepository.findAllByUserId(userId)).thenReturn(List.of(warehouseUser));
    when(orderRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(Page.empty());
    /* 2. Act */
    PaginatedResponse<OrderDTO> response = filterOrderUseCase.handle(jwtClaimDTO, request);
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
    FilterOrderRequest request = new FilterOrderRequest(null, 2, 5);
    Warehouse warehouse = createWarehouse();
    WarehouseUser warehouseUser = createWarehouseUser(userId, warehouse);
    when(warehouseUserRepository.findAllByUserId(userId)).thenReturn(List.of(warehouseUser));
    when(orderRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(Page.empty());
    /* 2. Act */
    filterOrderUseCase.handle(jwtClaimDTO, request);
    /* 3. Assert */
    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    verify(orderRepository).findAll(any(Specification.class), pageableCaptor.capture());
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
    FilterOrderRequest request = new FilterOrderRequest(null, 0, 10);
    when(warehouseUserRepository.findAllByUserId(userId)).thenReturn(Collections.emptyList());

    /* 2. Act & 3. Assert */
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> filterOrderUseCase.handle(jwtClaimDTO, request));
    assertEquals(NOT_TIED_TO_WAREHOUSE, exception.getMessage());
  }
}
