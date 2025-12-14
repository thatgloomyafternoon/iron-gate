package com.fw.irongate.web.api;

import static com.fw.irongate.constants.SystemConstants.OK;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class TestHealthController {

  @InjectMocks private HealthController healthController;

  @Test
  void whenCheckingHealth_assertOK() {
    ResponseEntity<String> resp = healthController.check();
    assertEquals(OK, resp.getBody());
  }
}
