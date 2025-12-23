package com.fw.irongate.web.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.models.dto.StockDTO;
import com.fw.irongate.usecases.filter_stock.FilterStockRequest;
import com.fw.irongate.usecases.filter_stock.FilterStockUseCase;
import com.fw.irongate.web.responses.PaginatedResponse;
import java.time.ZonedDateTime;
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
class TestApiStockFilter {

  @Mock private FilterStockUseCase filterStockUseCase;
  @InjectMocks private StockController stockController;

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
    String query = "product";
    Integer maxQuantity = 50;
    int page = 0;
    int size = 10;
    FilterStockRequest expectedRequest = new FilterStockRequest(query, maxQuantity, page, size);
    StockDTO stockDTO =
        new StockDTO(
            UUID.randomUUID(),
            "Product Name",
            "NYC",
            10,
            ZonedDateTime.now(),
            "admin",
            ZonedDateTime.now(),
            "admin");
    PaginatedResponse<StockDTO> mockResponse = new PaginatedResponse<>(List.of(stockDTO), 0, 1, 1);
    when(filterStockUseCase.handle(eq(mockJwt), eq(expectedRequest))).thenReturn(mockResponse);
    /* 2. Act */
    ResponseEntity<PaginatedResponse<StockDTO>> response =
        stockController.filter(mockJwt, query, maxQuantity, page, size);
    /* 3. Assert */
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().totalItems());
    verify(filterStockUseCase).handle(eq(mockJwt), eq(expectedRequest));
  }

  @Test
  void filter_ShouldHandleNullParameters() {
    /* 1. Arrange */
    int page = 0;
    int size = 10;
    /* Null query and maxQuantity */
    FilterStockRequest expectedRequest = new FilterStockRequest(null, null, page, size);
    PaginatedResponse<StockDTO> mockResponse = new PaginatedResponse<>(List.of(), 0, 0, 0);
    when(filterStockUseCase.handle(eq(mockJwt), eq(expectedRequest))).thenReturn(mockResponse);
    /* 2. Act */
    ResponseEntity<PaginatedResponse<StockDTO>> response =
        stockController.filter(mockJwt, null, null, page, size);
    /* 3. Assert */
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    verify(filterStockUseCase).handle(eq(mockJwt), eq(expectedRequest));
  }
}
