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

  /**
   * ERROR: GET https://gloomyafternoon.xyz/api/dashboard/stream net::ERR_INCOMPLETE_CHUNKED_ENCODING 200 (OK)<br><br>
   * The <code>ERR_INCOMPLETE_CHUNKED_ENCODING</code> error with SSE (Server-Sent Events) usually indicates that an intermediate proxy,
   * such as Nginx, is buffering the response.<br><br>
   * SSE requires a continuous stream of data. If Nginx buffers the chunks instead of passing them to the browser
   * immediately, the connection can time out or appear "incomplete" to the client.<br><br>
   * It is recommended to add the <code>X-Accel-Buffering: no</code> header to the <code>/api/dashboard/stream</code> response.
   * This header specifically tells Nginx to disable buffering for this request, allowing the SSE events to flow
   * through in real-time.
   */
  @GetMapping("/stream")
  public ResponseEntity<SseEmitter> stream() {
    return ResponseEntity.ok()
        .header("X-Accel-Buffering", "no")
        .body(streamDashboardUseCase.subscribe());
  }

  @GetMapping("/charts")
  public ResponseEntity<DashboardChartsDTO> getCharts() {
    return ResponseEntity.ok(getDashboardChartsUseCase.handle());
  }
}
