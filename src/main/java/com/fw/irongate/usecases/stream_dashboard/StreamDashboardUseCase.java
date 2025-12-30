package com.fw.irongate.usecases.stream_dashboard;

import com.fw.irongate.models.dto.DashboardEventDTO;
import com.fw.irongate.usecases.UseCase;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@UseCase
public class StreamDashboardUseCase {

  private static final Logger log = LoggerFactory.getLogger(StreamDashboardUseCase.class);
  private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

  public SseEmitter subscribe() {
    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
    emitters.add(emitter);
    emitter.onCompletion(() -> emitters.remove(emitter));
    emitter.onTimeout(() -> emitters.remove(emitter));
    emitter.onError((e) -> emitters.remove(emitter));
    return emitter;
  }

  public void broadcast(DashboardEventDTO event) {
    List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
    for (SseEmitter emitter : emitters) {
      try {
        emitter.send(event);
      } catch (Exception e) {
        deadEmitters.add(emitter);
        if (e instanceof AsyncRequestNotUsableException
            || (e instanceof IOException && "Broken pipe".equalsIgnoreCase(e.getMessage()))) {
          log.debug("Emitter disconnected: {}", e.getMessage());
        } else {
          log.warn("Failed to send event to emitter, removing it.", e);
        }
      }
    }
    emitters.removeAll(deadEmitters);
  }

  /**
   * The 504 Gateway Time-out error you are seeing suggests that the connection is being closed by the load balancer or proxy (like AWS ELB or
   * Nginx) because it perceives the connection as idle. Even with buffering disabled, if no data is sent for a certain period (often 60 seconds),
   * the connection may be dropped.<br><br>
   * To fix this, the proposal is to add a "heartbeat" mechanism to the <code>StreamDashboardUseCase</code>.<br><br>
   * The Plan:<br>
   * 1. Modify <code>StreamDashboardUseCase.java</code>: Add a <code>@Scheduled</code> method that runs every 25 seconds (well within the typical
   * 60-second timeout window).<br>
   * 2. Send Heartbeat: This method will send a simple comment event (e.g., :heartbeat) to all active SSE emitters. This traffic keeps the connection
   * alive and prevents the 504 timeout.<br>
   * 3. Cleanup: It will also help clean up any dead emitters that might have disconnected silently.<br><br>
   * This is a standard practice for maintaining long-lived SSE connections through proxies.
   */
  @Scheduled(fixedRate = 25000)
  public void sendHeartbeat() {
    List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
    for (SseEmitter emitter : emitters) {
      try {
        emitter.send(SseEmitter.event().comment("heartbeat"));
      } catch (Exception e) {
        deadEmitters.add(emitter);
        log.debug("Failed to send heartbeat, removing emitter: {}", e.getMessage());
      }
    }
    emitters.removeAll(deadEmitters);
  }
}
