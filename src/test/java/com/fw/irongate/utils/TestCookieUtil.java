package com.fw.irongate.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CookieUtilTest {

  @InjectMocks private CookieUtil cookieUtil;

  /* Define constants for testing */
  private static final String TEST_COOKIE_NAME = "auth_cookie";
  private static final long TEST_EXPIRY_MS = 900000L; /* 15 minutes (900,000 ms) */

  @BeforeEach
  void setUp() {
    /* Manually inject the private fields that Spring normally injects via @Value */
    ReflectionTestUtils.setField(cookieUtil, "expiryInMillisecond", TEST_EXPIRY_MS);
  }

  @Test
  void shouldCreateSecureHttpOnlyCookie() {
    /* --- Given --- */
    String jwtValue = "eyJhbGciOiJIUzI1NiJ9.test-token";
    /* --- When --- */
    ResponseCookie cookie = cookieUtil.createFreshCookie(jwtValue);
    /* --- Then --- */
    assertNotNull(cookie);
    /* 1. Verify Core Values */
    assertEquals(jwtValue, cookie.getValue());
    /* 2. Verify Security Flags (CRITICAL) */
    assertTrue(cookie.isHttpOnly(), "Cookie must be HttpOnly to prevent XSS");
    assertTrue(cookie.isSecure(), "Cookie must be Secure (HTTPS only)");
    assertEquals("Strict", cookie.getSameSite(), "Cookie must be Strict to prevent CSRF");
    /* 3. Verify Path */
    assertEquals("/", cookie.getPath());
    /* 4. Verify Max Age Calculation */
    /* Your code divides ms by 1000 to get seconds */
    long expectedSeconds = TEST_EXPIRY_MS / 1000;
    assertEquals(Duration.ofSeconds(expectedSeconds), cookie.getMaxAge());
  }
}
