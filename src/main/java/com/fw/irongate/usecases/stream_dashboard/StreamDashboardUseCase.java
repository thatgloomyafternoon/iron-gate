package com.fw.irongate.usecases.stream_dashboard;

import com.fw.irongate.models.dto.DashboardEventDTO;
import com.fw.irongate.usecases.UseCase;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@UseCase
public class StreamDashboardUseCase {

  private static final Logger log = LoggerFactory.getLogger(StreamDashboardUseCase.class);
  private final Map<String, SseEmitter> userEmitters = new HashMap<>();

  public SseEmitter subscribe(String userId) {
    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
    userEmitters.put(userId, emitter);
    Runnable removeEmitter = () -> userEmitters.remove(userId);
    emitter.onCompletion(removeEmitter);
    emitter.onTimeout(removeEmitter);
    emitter.onError((e) -> removeEmitter.run());
    return emitter;
  }

  public void removeUser(String userId) {
    userEmitters.remove(userId);
  }

  public void broadcast(DashboardEventDTO event) {
    userEmitters
        .values()
        .forEach(
            emitter -> {
              try {
                emitter.send(event);
              } catch (Exception e) {
                log.warn("Failed to send event to emitter, removing it.", e);
              }
            });
  }

  /**
   * The 504 Gateway Time-out error you are seeing suggests that the connection is being closed by the
   * load balancer or proxy (like AWS ELB or Nginx) because it perceives the connection as idle.
   * Even with buffering disabled, if no data is sent for a certain period (often 60 seconds), the
   * connection may be dropped.<br>
   * <br>
   * To fix this, the proposal is to add a "heartbeat" mechanism to the
   * <code>StreamDashboardUseCase</code>.<br>
   * <br>
   * The Plan:<br>
   * 1. Modify <code>StreamDashboardUseCase.java</code>: Add a <code>@Scheduled</code> method that
   * runs every 25 seconds (well within the typical 60-second timeout window).<br>
   * 2. Send Heartbeat: This method will send a simple comment event (e.g., :heartbeat) to all
   * active SSE emitters. This traffic keeps the connection alive and prevents the 504 timeout.<br>
   * 3. Cleanup: It will also help clean up any dead emitters that might have disconnected silently.
   * <br>
   * <br>
   * This is a standard practice for maintaining long-lived SSE connections through proxies.
   */
  @Scheduled(fixedRate = 25000)
  public void sendHeartbeat() {
    userEmitters
        .values()
        .forEach(
            emitter -> {
              try {
                emitter.send(SseEmitter.event().comment("heartbeat"));
              } catch (Exception e) {
                log.debug("Failed to send heartbeat, removing emitter: {}", e.getMessage());
              }
            });
  }
}
