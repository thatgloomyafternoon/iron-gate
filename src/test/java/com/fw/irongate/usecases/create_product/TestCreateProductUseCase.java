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

  /* Helper to create a dummy valid request */
  private CreateProductRequest createValidRequest() {
    return new CreateProductRequest(
        "Test Product", "SKU-123", "Description", new BigDecimal("100.00"), 10);
  }

  /* Helper to create dummy JWT claims */
  private JwtClaimDTO createJwtClaims() {
    return new JwtClaimDTO(
        UUID.randomUUID(), "admin@example.com", UUID.randomUUID(), "ROLE_ADMIN", "Admin User");
  }

  @Test
  void handle_ShouldCreateProduct_WhenSkuIsUnique() {
    /* 1. Arrange */
    CreateProductRequest request = createValidRequest();
    JwtClaimDTO jwt = createJwtClaims();
    UUID generatedId = UUID.randomUUID();
    /* Mock: SKU does not exist */
    when(productRepository.findBySku(request.sku())).thenReturn(Optional.empty());
    /* Mock: Save returns the product with a generated ID */
    /* We assume the Product entity has basic setters/getters and an setId method */
    when(productRepository.save(any(Product.class)))
        .thenAnswer(
            invocation -> {
              Product p = invocation.getArgument(0);
              p.setId(generatedId); /* Simulate DB generating UUID */
              return p;
            });
    /* 2. Act */
    IdResponse response = createProductUseCase.handle(request, jwt);
    /* 3. Assert */
    assertNotNull(response);
    assertEquals(generatedId, response.id());
    /* 4. Verify Data Mapping (Crucial step) */
    ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
    verify(productRepository).save(productCaptor.capture());
    Product capturedProduct = productCaptor.getValue();
    /* Check Business Fields */
    assertEquals(request.name(), capturedProduct.getName());
    assertEquals(request.sku(), capturedProduct.getSku());
    assertEquals(request.description(), capturedProduct.getDescription());
    assertEquals(request.price(), capturedProduct.getPrice());
    assertEquals(request.quantity(), capturedProduct.getQuantity());
    /* Check Audit Fields (from JWT) */
    assertEquals(jwt.email(), capturedProduct.getCreatedBy());
    assertEquals(jwt.email(), capturedProduct.getUpdatedBy());
  }

  @Test
  void handle_ShouldThrowException_WhenSkuAlreadyExists() {
    /* 1. Arrange */
    CreateProductRequest request = createValidRequest();
    JwtClaimDTO jwt = createJwtClaims();
    /* Mock: SKU ALREADY exists */
    when(productRepository.findBySku(request.sku())).thenReturn(Optional.of(new Product()));
    /* 2. Act & Assert */
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              createProductUseCase.handle(request, jwt);
            });
    /* Optional: Verify message content if you have access to MessageConstants in test scope */
    assertEquals(SKU_ALREADY_EXISTS, exception.getMessage());
    /* 3. Verify that SAVE was NEVER called */
    verify(productRepository, never()).save(any(Product.class));
  }
}
