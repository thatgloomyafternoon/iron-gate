package com.fw.irongate.usecases.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fw.irongate.models.dto.SecurityUserDTO;
import com.fw.irongate.utils.CookieUtil;
import com.fw.irongate.utils.JwtUtil;
import com.fw.irongate.web.requests.LoginRequest;
import com.github.f4b6a3.uuid.UuidCreator;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class TestLoginUseCase {

  @Mock private AuthenticationManager authenticationManager;
  @Mock private JwtUtil jwtUtil;
  @Mock private CookieUtil cookieUtil;
  @InjectMocks private LoginUseCase loginUseCase;

  @Test
  void shouldLoginSuccessfully_AndReturnCookie() {
    /* --- Given --- */
    LoginRequest request = new LoginRequest("test@example.com", "password123");
    /* 1. Create the detailed User DTO that comes from the database */
    UUID id = UuidCreator.getTimeOrderedEpoch();
    UUID roleId = UuidCreator.getTimeOrderedEpoch();
    SecurityUserDTO mockUser =
        new SecurityUserDTO(
            id,
            "test@example.com",
            "hashedPass",
            roleId,
            "ROLE_ADMIN",
            "John Doe",
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    /* 2. Mock Authentication to return this User DTO */
    Authentication auth = mock(Authentication.class);
    given(auth.getPrincipal()).willReturn(mockUser);
    given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .willReturn(auth);
    /* 3. Mock JWT generation logic */
    String expectedJwt = "generated-jwt-token-string";
    given(jwtUtil.generateJwt(any(), any(), any(), any(), any())).willReturn(expectedJwt);
    /* 4. Mock Cookie creation */
    ResponseCookie expectedCookie = ResponseCookie.from("auth_cookie", expectedJwt).build();
    given(cookieUtil.createFreshCookie(expectedJwt)).willReturn(expectedCookie);
    /* --- When --- */
    ResponseCookie actualCookie = loginUseCase.handle(request);
    /* --- Then --- */
    assertEquals(expectedCookie, actualCookie);
    /* CRITICAL: Verify parameter mapping */
    /* We ensure that DTO fields are mapped to generateJwt() arguments in the CORRECT ORDER */
    verify(jwtUtil)
        .generateJwt(
            eq(id.toString()), // 1st arg: ID (toString)
            eq("test@example.com"), // 2nd arg: Email
            eq(roleId.toString()), // 3rd arg: Role ID (toString)
            eq("ROLE_ADMIN"), // 4th arg: Role Name
            eq("John Doe") // 5th arg: Full Name
            );
  }

  @Test
  void shouldThrowException_WhenCredentialsInvalid() {
    /* --- Given --- */
    LoginRequest request = new LoginRequest("wrong@email.com", "wrongPass");
    /* Simulate Auth Failure */
    given(authenticationManager.authenticate(any()))
        .willThrow(new BadCredentialsException("Bad credentials"));
    /* --- When & Then --- */
    assertThrows(BadCredentialsException.class, () -> loginUseCase.handle(request));
    /* Verify we never tried to generate a token */
    verify(jwtUtil, org.mockito.Mockito.never()).generateJwt(any(), any(), any(), any(), any());
  }
}
