package com.fw.irongate.web.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.usecases.create_stock.CreateStockRequest;
import com.fw.irongate.usecases.create_stock.CreateStockUseCase;
import com.fw.irongate.web.responses.IdResponse;
import java.math.BigInteger;
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
class TestApiStockCreate {

  @Mock private CreateStockUseCase createStockUseCase;
  @InjectMocks private StockController stockController;

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
    CreateStockRequest request =
        new CreateStockRequest(UUID.randomUUID(), UUID.randomUUID(), new BigInteger("100"));
    UUID expectedId = UUID.randomUUID();
    when(createStockUseCase.handle(mockJwt, request)).thenReturn(new IdResponse(expectedId));
    /* 2. Act */
    ResponseEntity<IdResponse> response = stockController.create(mockJwt, request);
    /* 3. Assert */
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(expectedId, response.getBody().id());
    verify(createStockUseCase).handle(mockJwt, request);
  }

  @Test
  void create_ShouldPropagateException_WhenUseCaseThrows() {
    /* 1. Arrange */
    CreateStockRequest request =
        new CreateStockRequest(UUID.randomUUID(), UUID.randomUUID(), new BigInteger("100"));
    String errorMessage = "Some business error";
    when(createStockUseCase.handle(mockJwt, request))
        .thenThrow(new IllegalArgumentException(errorMessage));
    /* 2. Act & Assert */
    assertThrows(IllegalArgumentException.class, () -> stockController.create(mockJwt, request));
    verify(createStockUseCase).handle(mockJwt, request);
  }
}
