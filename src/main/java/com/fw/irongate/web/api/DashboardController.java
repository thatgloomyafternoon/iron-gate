package com.fw.irongate.web.api;

import com.fw.irongate.models.dto.DashboardChartsDTO;
import com.fw.irongate.usecases.get_dashboard_charts.GetDashboardChartsUseCase;
import com.fw.irongate.usecases.stream_dashboard.StreamDashboardUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

  private final StreamDashboardUseCase streamDashboardUseCase;
  private final GetDashboardChartsUseCase getDashboardChartsUseCase;

  public DashboardController(
      StreamDashboardUseCase streamDashboardUseCase,
      GetDashboardChartsUseCase getDashboardChartsUseCase) {
    this.streamDashboardUseCase = streamDashboardUseCase;
    this.getDashboardChartsUseCase = getDashboardChartsUseCase;
  }

  @GetMapping("/stream")
  public SseEmitter stream() {
    return streamDashboardUseCase.subscribe();
  }

  @GetMapping("/charts")
  public ResponseEntity<DashboardChartsDTO> getCharts() {
    return ResponseEntity.ok(getDashboardChartsUseCase.handle());
  }
}
