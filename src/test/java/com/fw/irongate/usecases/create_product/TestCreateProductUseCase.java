package com.fw.irongate.usecases.create_product;

import static com.fw.irongate.constants.MessageConstants.SKU_ALREADY_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.models.entities.Product;
import com.fw.irongate.repositories.ProductRepository;
import com.fw.irongate.web.responses.IdResponse;
import com.github.f4b6a3.uuid.UuidCreator;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateProductUseCaseTest {

  @Mock private ProductRepository productRepository;

  @InjectMocks private CreateProductUseCase createProductUseCase;

  @Test
  void shouldCreateProductSuccessfully_WhenSkuIsUnique() {
    /* 1. Arrange (Prepare data) */
    CreateProductRequest request =
        new CreateProductRequest("Test Product", "SKU-123", "Desc", new BigDecimal("100.00"), 10);
    JwtClaimDTO userClaims =
        new JwtClaimDTO(
            UuidCreator.getTimeOrderedEpoch(),
            "user@example.com",
            UuidCreator.getTimeOrderedEpoch(),
            "role",
            "full name");
    /* Mock: Repository finds nothing for this SKU (it's unique) */
    when(productRepository.findBySku("SKU-123")).thenReturn(Optional.empty());
    /* Mock: Repository returns a saved product with an ID */
    UUID productId = UuidCreator.getTimeOrderedEpoch();
    Product savedProduct = new Product();
    savedProduct.setId(productId);
    when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
    /* 2. Act (Run the method) */
    IdResponse response = createProductUseCase.handle(request, userClaims);
    /* 3. Assert (Verify results) */
    assertNotNull(response);
    assertEquals(productId, response.id());
    /* Verify that save was called with the correct data */
    ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
    verify(productRepository).save(productCaptor.capture());
    Product capturedProduct = productCaptor.getValue();
    assertEquals("user@example.com", capturedProduct.getCreatedBy());
    assertEquals("SKU-123", capturedProduct.getSku());
    assertEquals(new BigDecimal("100.00"), capturedProduct.getPriceUsd());
  }

  @Test
  void shouldThrowException_WhenSkuAlreadyExists() {
    /* 1. Arrange */
    CreateProductRequest request =
        new CreateProductRequest("Duplicate Product", "SKU-EXISTING", "Desc", BigDecimal.TEN, 5);
    JwtClaimDTO userClaims =
        new JwtClaimDTO(
            UuidCreator.getTimeOrderedEpoch(),
            "user@example.com",
            UuidCreator.getTimeOrderedEpoch(),
            "role",
            "full name");
    /* Mock: Repository finds an EXISTING product */
    when(productRepository.findBySku("SKU-EXISTING")).thenReturn(Optional.of(new Product()));
    /* 2. Act & Assert */
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> createProductUseCase.handle(request, userClaims));
    /* Check the error message (Assuming SKU_ALREADY_EXISTS matches your constant) */
    /* If your constant is public static, use CreateProductUseCase.SKU_ALREADY_EXISTS */
    assertEquals(SKU_ALREADY_EXISTS, exception.getMessage());
    /* 3. Verify that save was NEVER called */
    verify(productRepository, never()).save(any());
  }
}
