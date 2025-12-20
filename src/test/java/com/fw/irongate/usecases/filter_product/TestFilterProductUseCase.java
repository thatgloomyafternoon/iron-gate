package com.fw.irongate.usecases.filter_product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fw.irongate.models.dto.ProductDTO;
import com.fw.irongate.models.entities.Product;
import com.fw.irongate.repositories.ProductRepository;
import com.fw.irongate.web.responses.PaginatedResponse;
import com.github.f4b6a3.uuid.UuidCreator;
import java.math.BigDecimal;
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
class FilterProductUseCaseTest {

  @Mock private ProductRepository productRepository;

  @InjectMocks private FilterProductUseCase filterProductUseCase;

  /* Helper to create a dummy Product entity */
  private Product createProduct() {
    Product product = new Product();
    product.setId(UuidCreator.getTimeOrderedEpoch()); /* Assuming BaseEntity provides setId/getId */
    product.setName("Test Product");
    product.setSku("SKU-001");
    product.setDescription("Description");
    product.setPrice(new BigDecimal("99.99"));
    product.setQuantity(10);
    return product;
  }

  @SuppressWarnings("unchecked")
  @Test
  void handle_ShouldReturnMappedData_WhenProductsExist() {
    /* 1. Arrange */
    FilterProductRequest request = new FilterProductRequest("test", null, null, null, 0, 10);
    Product product = createProduct();
    Page<Product> productPage = new PageImpl<>(List.of(product));
    /* Mock repository to return the page */
    when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(productPage);
    /* 2. Act */
    PaginatedResponse<ProductDTO> response = filterProductUseCase.handle(request);
    /* 3. Assert */
    assertNotNull(response);
    assertEquals(1, response.totalItems());
    assertEquals(1, response.totalPages());
    assertEquals(0, response.currentPage());
    /* Verify Content Mapping */
    ProductDTO dto = response.data().getFirst();
    assertEquals(product.getId(), dto.id());
    assertEquals(product.getName(), dto.name());
    assertEquals(product.getSku(), dto.sku());
    assertEquals(product.getPrice(), dto.price());
    assertEquals(product.getQuantity(), dto.quantity());
  }

  @SuppressWarnings("unchecked")
  @Test
  void handle_ShouldReturnEmptyList_WhenNoProductsFound() {
    /* 1. Arrange */
    FilterProductRequest request = new FilterProductRequest("nonexistent", null, null, null, 0, 10);
    /* Mock repository to return empty page */
    when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(Page.empty());
    /* 2. Act */
    PaginatedResponse<ProductDTO> response = filterProductUseCase.handle(request);
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
    FilterProductRequest request = new FilterProductRequest(null, null, null, null, 2, 5);
    when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(Page.empty());
    /* 2. Act */
    filterProductUseCase.handle(request);
    /* 3. Assert - Capture the Pageable passed to the repository */
    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    verify(productRepository).findAll(any(Specification.class), pageableCaptor.capture());
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
