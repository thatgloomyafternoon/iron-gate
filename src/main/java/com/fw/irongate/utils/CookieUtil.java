package com.fw.irongate.utils;

import static com.fw.irongate.constants.SystemConstants.COOKIE_NAME;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

  @Value("${jwt.expiry_in_millisecond}")
  private long expiryInMillisecond;

  public ResponseCookie createFreshCookie(String value) {
    return ResponseCookie.from(COOKIE_NAME, value)
        .httpOnly(true)
        .secure(true)
        .sameSite("Strict")
        .path("/")
        .maxAge(expiryInMillisecond / 1000)
        .build();
  }

  public ResponseCookie createExpiredCookie() {
    return ResponseCookie.from(COOKIE_NAME, null)
        .path("/")
        .maxAge(0) /* Set to 0 to invalidate the cookie */
        .build();
  }
}
