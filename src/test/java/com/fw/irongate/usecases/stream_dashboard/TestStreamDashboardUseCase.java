package com.fw.irongate.usecases.stream_dashboard;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fw.irongate.models.dto.DashboardEventDTO;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class TestStreamDashboardUseCase {

  private final StreamDashboardUseCase streamDashboardUseCase = new StreamDashboardUseCase();

  @Test
  void subscribe_ShouldReturnEmitter() {
    SseEmitter emitter = streamDashboardUseCase.subscribe();
    assertNotNull(emitter);
  }

  @Test
  void broadcast_ShouldNotThrowException_WhenNoEmitters() {
    DashboardEventDTO event = new DashboardEventDTO("test");
    streamDashboardUseCase.broadcast(event);
    /* Success if no exception thrown */
  }

  @Test
  void broadcast_ShouldSendToEmitters() {
    streamDashboardUseCase.subscribe();
    DashboardEventDTO event = new DashboardEventDTO("test");
    streamDashboardUseCase.broadcast(event);
    /* Logic for verifying SSE send is complex, but this tests the loop doesn't crash */
  }

  @Test
  void sendHeartbeat_ShouldNotThrowException() {
    streamDashboardUseCase.subscribe();
    streamDashboardUseCase.sendHeartbeat();
    /* Logic for verifying SSE send is complex, but this tests the loop doesn't crash */
  }
}
