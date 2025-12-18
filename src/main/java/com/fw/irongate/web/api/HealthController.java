package com.fw.irongate.web.api;

import static com.fw.irongate.constants.SystemConstants.OK;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

  @GetMapping("/check")
  public ResponseEntity<String> check() {
    return ResponseEntity.ok(OK);
  }
}
