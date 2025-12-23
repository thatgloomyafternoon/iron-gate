package com.fw.irongate.web.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fw.irongate.models.dto.WarehouseDTO;
import com.fw.irongate.usecases.filter_warehouse.FilterWarehouseRequest;
import com.fw.irongate.usecases.filter_warehouse.FilterWarehouseUseCase;
import com.fw.irongate.web.responses.PaginatedResponse;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class TestApiWarehouseFilter {

  @Mock private FilterWarehouseUseCase filterWarehouseUseCase;
  @InjectMocks private WarehouseController warehouseController;

  @Test
  void filter_ShouldReturn200AndData_WhenSuccessful() {
    /* 1. Arrange */
    String query = "NYC";
    int page = 0;
    int size = 10;
    FilterWarehouseRequest expectedRequest = new FilterWarehouseRequest(query, page, size);
    WarehouseDTO warehouseDTO =
        new WarehouseDTO(UUID.randomUUID(), "New York", "NYC", ZonedDateTime.now(), "admin");
    PaginatedResponse<WarehouseDTO> mockResponse =
        new PaginatedResponse<>(List.of(warehouseDTO), 0, 1, 1);
    when(filterWarehouseUseCase.handle(eq(expectedRequest))).thenReturn(mockResponse);
    /* 2. Act */
    ResponseEntity<PaginatedResponse<WarehouseDTO>> response =
        warehouseController.filter(query, page, size);
    /* 3. Assert */
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().totalItems());
    verify(filterWarehouseUseCase).handle(eq(expectedRequest));
  }

  @Test
  void filter_ShouldHandleNullQuery() {
    /* 1. Arrange */
    int page = 0;
    int size = 10;
    /* Null query */
    FilterWarehouseRequest expectedRequest = new FilterWarehouseRequest(null, page, size);
    PaginatedResponse<WarehouseDTO> mockResponse = new PaginatedResponse<>(List.of(), 0, 0, 0);
    when(filterWarehouseUseCase.handle(eq(expectedRequest))).thenReturn(mockResponse);
    /* 2. Act */
    ResponseEntity<PaginatedResponse<WarehouseDTO>> response =
        warehouseController.filter(null, page, size);
    /* 3. Assert */
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    verify(filterWarehouseUseCase).handle(eq(expectedRequest));
  }
}
