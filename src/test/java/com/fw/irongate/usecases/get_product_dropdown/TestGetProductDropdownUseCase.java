package com.fw.irongate.usecases.get_product_dropdown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fw.irongate.models.dto.ProductDropdownDTO;
import com.fw.irongate.models.entities.Product;
import com.fw.irongate.repositories.ProductRepository;
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
class TestGetProductDropdownUseCase {

  @Mock private ProductRepository productRepository;
  @InjectMocks private GetProductDropdownUseCase getProductDropdownUseCase;

  @Test
  void handle_ShouldReturnListOfProductDropdownDTO_WhenProductsExist() {
    /* Arrange */
    UUID id1 = UuidCreator.getTimeOrderedEpoch();
    UUID id2 = UuidCreator.getTimeOrderedEpoch();
    Product product1 = new Product();
    product1.setId(id1);
    product1.setName("Product A");
    Product product2 = new Product();
    product2.setId(id2);
    product2.setName("Product B");
    when(productRepository.findAll()).thenReturn(List.of(product1, product2));
    /* Act */
    List<ProductDropdownDTO> result = getProductDropdownUseCase.handle();
    /* Assert */
    assertEquals(2, result.size());
    assertEquals(id1, result.get(0).productId());
    assertEquals("Product A", result.get(0).productName());
    assertEquals(id2, result.get(1).productId());
    assertEquals("Product B", result.get(1).productName());
    verify(productRepository).findAll();
  }

  @Test
  void handle_ShouldReturnEmptyList_WhenNoProductsExist() {
    /* Arrange */
    when(productRepository.findAll()).thenReturn(Collections.emptyList());
    /* Act */
    List<ProductDropdownDTO> result = getProductDropdownUseCase.handle();
    /* Assert */
    assertTrue(result.isEmpty());
    verify(productRepository).findAll();
  }
}
