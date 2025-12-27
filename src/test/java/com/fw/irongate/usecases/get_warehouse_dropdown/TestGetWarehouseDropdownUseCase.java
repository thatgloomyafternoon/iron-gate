package com.fw.irongate.usecases.get_warehouse_dropdown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fw.irongate.models.dto.WarehouseDropdownDTO;
import com.fw.irongate.models.entities.Warehouse;
import com.fw.irongate.repositories.WarehouseRepository;
import com.github.f4b6a3.uuid.UuidCreator;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TestGetWarehouseDropdownUseCase {

  @Mock private WarehouseRepository warehouseRepository;
  @InjectMocks private GetWarehouseDropdownUseCase getWarehouseDropdownUseCase;

  @Test
  void handle_ShouldReturnListOfWarehouseDropdownDTO_WhenWarehousesExist() {
    /* Arrange */
    UUID id1 = UuidCreator.getTimeOrderedEpoch();
    UUID id2 = UuidCreator.getTimeOrderedEpoch();
    Warehouse warehouse1 = new Warehouse();
    warehouse1.setId(id1);
    warehouse1.setName("Warehouse A");
    Warehouse warehouse2 = new Warehouse();
    warehouse2.setId(id2);
    warehouse2.setName("Warehouse B");
    when(warehouseRepository.findAll()).thenReturn(List.of(warehouse1, warehouse2));
    /* Act */
    List<WarehouseDropdownDTO> result = getWarehouseDropdownUseCase.handle();
    /* Assert */
    assertEquals(2, result.size());
    assertEquals(id1, result.get(0).warehouseId());
    assertEquals("Warehouse A", result.get(0).warehouseName());
    assertEquals(id2, result.get(1).warehouseId());
    assertEquals("Warehouse B", result.get(1).warehouseName());
    verify(warehouseRepository, times(1)).findAll();
  }

  @Test
  void handle_ShouldReturnEmptyList_WhenNoWarehousesExist() {
    /* Arrange */
    when(warehouseRepository.findAll()).thenReturn(Collections.emptyList());
    /* Act */
    List<WarehouseDropdownDTO> result = getWarehouseDropdownUseCase.handle();
    /* Assert */
    assertTrue(result.isEmpty());
    verify(warehouseRepository, times(1)).findAll();
  }

  @Test
  void handle_ShouldPropagateException_WhenRepositoryThrowsException() {
    /* Arrange */
    RuntimeException expectedException = new RuntimeException("DB Connection failed");
    when(warehouseRepository.findAll()).thenThrow(expectedException);
    /* Act & Assert */
    RuntimeException actualException =
        assertThrows(RuntimeException.class, () -> getWarehouseDropdownUseCase.handle());
    assertEquals(expectedException.getMessage(), actualException.getMessage());
    verify(warehouseRepository, times(1)).findAll();
  }
}
