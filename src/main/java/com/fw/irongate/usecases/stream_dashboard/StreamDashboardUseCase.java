package com.fw.irongate.usecases.stream_dashboard;

import com.fw.irongate.models.dto.DashboardEventDTO;
import com.fw.irongate.usecases.UseCase;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
}
