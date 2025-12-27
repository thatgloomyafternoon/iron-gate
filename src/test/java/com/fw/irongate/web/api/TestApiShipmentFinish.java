package com.fw.irongate.web.api;

import static com.fw.irongate.constants.SystemConstants.OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.usecases.finish_shipment.FinishShipmentUseCase;
import com.fw.irongate.web.responses.MessageResponse;
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
class TestApiShipmentFinish {

  @Mock private FinishShipmentUseCase finishShipmentUseCase;
  @InjectMocks private ShipmentController shipmentController;

  private JwtClaimDTO mockJwt;

  @BeforeEach
  void setUp() {
    mockJwt =
        new JwtClaimDTO(
            UUID.randomUUID(), "driver@example.com", UUID.randomUUID(), "ROLE_DRIVER", "Driver");
  }

  @Test
  void finish_ShouldReturn200AndMessage_WhenSuccessful() {
    /* 1. Arrange */
    UUID shipmentId = UUID.randomUUID();
    when(finishShipmentUseCase.handle(mockJwt, shipmentId)).thenReturn(new MessageResponse(OK));
    /* 2. Act */
    ResponseEntity<MessageResponse> response = shipmentController.finish(mockJwt, shipmentId);
    /* 3. Assert */
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(OK, response.getBody().message());
    /* Verify delegation */
    verify(finishShipmentUseCase).handle(mockJwt, shipmentId);
  }
}
