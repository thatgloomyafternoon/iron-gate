package com.fw.irongate.web.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.usecases.create_product.CreateProductRequest;
import com.fw.irongate.usecases.create_product.CreateProductUseCase;
import com.fw.irongate.web.responses.IdResponse;
import java.math.BigDecimal;
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
class TestApiProductCreate {

  @Mock private CreateProductUseCase createProductUseCase;
  @InjectMocks private ProductController productController;

  private JwtClaimDTO mockJwt;

  @BeforeEach
  void setUp() {
    mockJwt =
        new JwtClaimDTO(
            UUID.randomUUID(), "test@example.com", UUID.randomUUID(), "ROLE_ADMIN", "Admin");
  }

  @Test
  void create_ShouldReturn200AndId_WhenSuccessful() {
    /* 1. Arrange */
    CreateProductRequest request =
        new CreateProductRequest("Name", "SKU", "Desc", BigDecimal.TEN, 10);
    UUID expectedId = UUID.randomUUID();
    when(createProductUseCase.handle(request, mockJwt)).thenReturn(new IdResponse(expectedId));
    /* 2. Act (Direct method call - no MockMvc) */
    ResponseEntity<IdResponse> response = productController.create(mockJwt, request);
    /* 3. Assert */
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(expectedId, response.getBody().id());
    /* Verify delegation */
    verify(createProductUseCase).handle(request, mockJwt);
  }
}
