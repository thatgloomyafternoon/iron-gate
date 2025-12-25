package com.fw.irongate.web.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.models.dto.OrderDTO;
import com.fw.irongate.usecases.filter_order.FilterOrderRequest;
import com.fw.irongate.usecases.filter_order.FilterOrderUseCase;
import com.fw.irongate.usecases.fulfill_order.FulfillOrderUseCase;
import com.fw.irongate.web.responses.PaginatedResponse;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class TestApiOrderFilter {

  @Mock private FilterOrderUseCase filterOrderUseCase;
  @Mock private FulfillOrderUseCase fulfillOrderUseCase;
  @InjectMocks private OrderController orderController;

  private JwtClaimDTO mockJwt;

  @BeforeEach
  void setUp() {
    mockJwt =
        new JwtClaimDTO(
            UUID.randomUUID(), "test@example.com", UUID.randomUUID(), "ROLE_ADMIN", "Admin");
  }

  @Test
  void filter_ShouldReturn200AndData_WhenSuccessful() {
    /* 1. Arrange */
    String query = "customer";
    int page = 0;
    int size = 10;
    FilterOrderRequest expectedRequest = new FilterOrderRequest(query, page, size);
    OrderDTO orderDTO =
        new OrderDTO(
            UUID.randomUUID(),
            "Customer Name",
            Collections.emptyList(),
            BigDecimal.TEN,
            "NYC",
            "CREATED",
            ZonedDateTime.now(),
            "admin");
    PaginatedResponse<OrderDTO> mockResponse = new PaginatedResponse<>(List.of(orderDTO), 0, 1, 1);
    when(filterOrderUseCase.handle(eq(mockJwt), eq(expectedRequest))).thenReturn(mockResponse);
    /* 2. Act */
    ResponseEntity<PaginatedResponse<OrderDTO>> response =
        orderController.filter(mockJwt, query, page, size);
    /* 3. Assert */
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().totalItems());
    verify(filterOrderUseCase).handle(eq(mockJwt), eq(expectedRequest));
  }

  @Test
  void filter_ShouldHandleNullParameters() {
    /* 1. Arrange */
    int page = 0;
    int size = 10;
    /* Null query */
    // Note: The controller defines default value as empty string for query if missing,
    // but if passed explicitly as null (which can happen in Java method call, though Spring might
    // convert it),
    // let's check what value we expect. The controller says:
    // @RequestParam(name = "query", required = false, defaultValue = "") String query
    // So if I call the method with null, it receives null.
    // If I call the API without the param, Spring passes "".

    // In this unit test I am calling the method directly.
    FilterOrderRequest expectedRequest = new FilterOrderRequest(null, page, size);
    PaginatedResponse<OrderDTO> mockResponse = new PaginatedResponse<>(List.of(), 0, 0, 0);
    when(filterOrderUseCase.handle(eq(mockJwt), eq(expectedRequest))).thenReturn(mockResponse);
    /* 2. Act */
    ResponseEntity<PaginatedResponse<OrderDTO>> response =
        orderController.filter(mockJwt, null, page, size);
    /* 3. Assert */
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    verify(filterOrderUseCase).handle(eq(mockJwt), eq(expectedRequest));
  }
}
