package com.fw.irongate.usecases.logout;

import static com.fw.irongate.constants.SystemConstants.COOKIE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.models.entities.RevokedToken;
import com.fw.irongate.repositories.RevokedTokenRepository;
import com.fw.irongate.utils.CookieUtil;
import com.fw.irongate.utils.JwtUtil;
import com.github.f4b6a3.uuid.UuidCreator;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;

@ExtendWith(MockitoExtension.class)
class TestLogoutUseCase {

  @Mock private JwtUtil jwtUtil;
  @Mock private RevokedTokenRepository revokedTokenRepository;
  @Mock private CookieUtil cookieUtil;
  @InjectMocks private LogoutUseCase logoutUseCase;
  @Captor private ArgumentCaptor<RevokedToken> revokedTokenCaptor;

  @Test
  void handle_ShouldSaveRevokedTokenAndReturnEmptyCookie_WhenTokenIsValid() {
    /* 1. Arrange */
    String token = "valid.jwt.token";
    String email = "user@example.com";
    /* Assuming JwtClaimDTO is a Java Record based on .email() syntax */
    JwtClaimDTO claimDTO =
        new JwtClaimDTO(
            UuidCreator.getTimeOrderedEpoch(),
            email,
            UuidCreator.getTimeOrderedEpoch(),
            "role",
            "full name");
    Instant mockExpiration = Instant.now().plusSeconds(3600);
    ResponseCookie expectedCookie = ResponseCookie.from("auth-cookie", "").build();
    /* Mock extracting expiration */
    when(jwtUtil.extractExpiration(token)).thenReturn(mockExpiration);
    /* Mock cookie creation */
    when(cookieUtil.createEmptyCookie()).thenReturn(expectedCookie);
    /* 2. Act */
    ResponseCookie result = logoutUseCase.handle(claimDTO, token);
    /* 3. Assert */
    assertEquals(expectedCookie, result);
    /* Verify repository save was called */
    verify(revokedTokenRepository).save(revokedTokenCaptor.capture());
    /* Check the internal state of the saved object */
    RevokedToken capturedToken = revokedTokenCaptor.getValue();
    assertEquals(token, capturedToken.getJwt());
    assertEquals(email, capturedToken.getCreatedBy());
    assertEquals(email, capturedToken.getUpdatedBy());
  }

  @Test
  void handle_ShouldSwallowExceptionAndReturnEmptyCookie_WhenErrorOccurs() {
    /* 1. Arrange */
    String token = "broken.jwt.token";
    JwtClaimDTO claimDTO =
        new JwtClaimDTO(
            UuidCreator.getTimeOrderedEpoch(),
            "user@example.com",
            UuidCreator.getTimeOrderedEpoch(),
            "role",
            "full name");
    ResponseCookie expectedCookie = ResponseCookie.from(COOKIE_NAME, "").build();
    /* Simulate an exception (e.g., JWT parsing fails or DB connection error) */
    when(jwtUtil.extractExpiration(token)).thenThrow(new RuntimeException("JWT Parsing Failed"));
    /* Even if the try-block fails, we expect cookieUtil to be called */
    when(cookieUtil.createEmptyCookie()).thenReturn(expectedCookie);
    /* 2. Act */
    /* The method catches Exception, logs it, and continues. It should NOT throw. */
    ResponseCookie result = logoutUseCase.handle(claimDTO, token);
    /* 3. Assert */
    assertEquals(expectedCookie, result);
    /* Verify we never tried to save anything because the exception happened before save() */
    verify(revokedTokenRepository, never()).save(any());
  }
}
