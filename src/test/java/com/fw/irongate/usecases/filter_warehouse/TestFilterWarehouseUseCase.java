package com.fw.irongate.usecases.filter_warehouse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fw.irongate.models.dto.WarehouseDTO;
import com.fw.irongate.models.entities.Warehouse;
import com.fw.irongate.repositories.WarehouseRepository;
import com.fw.irongate.web.responses.PaginatedResponse;
import com.github.f4b6a3.uuid.UuidCreator;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
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
class FilterWarehouseUseCaseTest {

  @Mock private WarehouseRepository warehouseRepository;
  @InjectMocks private FilterWarehouseUseCase filterWarehouseUseCase;

  /* Helper to create a dummy Warehouse entity */
  private Warehouse createWarehouse() {
    Warehouse warehouse = new Warehouse();
    warehouse.setId(UuidCreator.getTimeOrderedEpoch());
    warehouse.setCreatedAt(ZonedDateTime.now());
    warehouse.setCreatedBy("system@mail.com");
    warehouse.setCity("New York");
    warehouse.setCode("NYC");
    return warehouse;
  }

  @SuppressWarnings("unchecked")
  @Test
  void handle_ShouldReturnMappedData_WhenWarehousesExist() {
    /* 1. Arrange */
    FilterWarehouseRequest request = new FilterWarehouseRequest("test", 0, 10);
    Warehouse warehouse = createWarehouse();
    Page<Warehouse> warehousePage = new PageImpl<>(List.of(warehouse));
    /* Mock repository to return the page */
    when(warehouseRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(warehousePage);
    /* 2. Act */
    PaginatedResponse<WarehouseDTO> response = filterWarehouseUseCase.handle(request);
    /* 3. Assert */
    assertNotNull(response);
    assertEquals(1, response.totalItems());
    assertEquals(1, response.totalPages());
    assertEquals(0, response.currentPage());
    /* Verify Content Mapping */
    WarehouseDTO dto = response.data().getFirst();
    assertEquals(warehouse.getId(), dto.id());
    assertEquals(warehouse.getCity(), dto.city());
    assertEquals(warehouse.getCode(), dto.code());
    assertEquals(warehouse.getCreatedAt(), dto.createdAt());
    assertEquals(warehouse.getCreatedBy(), dto.createdBy());
  }

  @SuppressWarnings("unchecked")
  @Test
  void handle_ShouldReturnEmptyList_WhenNoWarehousesFound() {
    /* 1. Arrange */
    FilterWarehouseRequest request = new FilterWarehouseRequest("nonexistent", 0, 10);
    /* Mock repository to return empty page */
    when(warehouseRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(Page.empty());
    /* 2. Act */
    PaginatedResponse<WarehouseDTO> response = filterWarehouseUseCase.handle(request);
    /* 3. Assert */
    assertNotNull(response);
    assertTrue(response.data().isEmpty());
    assertEquals(0, response.totalItems());
  }

  @SuppressWarnings("unchecked")
  @Test
  void handle_ShouldApplyCorrectPaginationAndSorting() {
    /* 1. Arrange */
    /* Requesting Page 2 (index 2), Size 5 */
    FilterWarehouseRequest request = new FilterWarehouseRequest(null, 2, 5);
    when(warehouseRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(Page.empty());
    /* 2. Act */
    filterWarehouseUseCase.handle(request);
    /* 3. Assert - Capture the Pageable passed to the repository */
    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    verify(warehouseRepository).findAll(any(Specification.class), pageableCaptor.capture());
    Pageable capturedPageable = pageableCaptor.getValue();
    /* Check Page Number and Size */
    assertEquals(2, capturedPageable.getPageNumber());
    assertEquals(5, capturedPageable.getPageSize());
    /* Check Sorting (Must be createdAt DESC) */
    Sort sort = capturedPageable.getSort();
    assertNotNull(sort.getOrderFor("createdAt"));
    assertTrue(Objects.requireNonNull(sort.getOrderFor("createdAt")).isDescending());
  }
}
