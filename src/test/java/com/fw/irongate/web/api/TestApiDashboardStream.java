package com.fw.irongate.web.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.usecases.stream_dashboard.StreamDashboardUseCase;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
class TestApiDashboardStream {

  @Mock private StreamDashboardUseCase streamDashboardUseCase;
  @InjectMocks private DashboardController dashboardController;

  @Test
  void stream_ShouldReturnSseEmitter() {
    /* 1. Arrange */
    SseEmitter expectedEmitter = new SseEmitter();
    UUID userId = UUID.randomUUID();
    JwtClaimDTO jwtClaimDTO =
        new JwtClaimDTO(userId, "test@email.com", UUID.randomUUID(), "ROLE_USER", "Test User");
    when(streamDashboardUseCase.subscribe(userId.toString())).thenReturn(expectedEmitter);
    /* 2. Act */
    ResponseEntity<SseEmitter> result = dashboardController.stream(jwtClaimDTO);
    /* 3. Assert */
    assertNotNull(result);
    assertEquals(expectedEmitter, result.getBody());
    assertEquals("no", result.getHeaders().getFirst("X-Accel-Buffering"));
  }
}
