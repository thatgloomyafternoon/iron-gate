package com.fw.irongate.web.api;

import com.fw.irongate.usecases.stream_dashboard.StreamDashboardUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

  private final StreamDashboardUseCase streamDashboardUseCase;

  public DashboardController(StreamDashboardUseCase streamDashboardUseCase) {
    this.streamDashboardUseCase = streamDashboardUseCase;
  }

  @GetMapping("/stream")
  public SseEmitter stream() {
    return streamDashboardUseCase.subscribe();
  }
}
