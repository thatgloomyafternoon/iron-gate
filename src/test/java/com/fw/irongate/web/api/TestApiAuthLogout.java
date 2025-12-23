package com.fw.irongate.web.api;

import static com.fw.irongate.constants.SystemConstants.COOKIE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.usecases.logout.LogoutUseCase;
import com.fw.irongate.web.responses.MessageResponse;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class TestApiAuthLogout {

  @Mock private LogoutUseCase logoutUseCase;
  @Mock private HttpServletResponse httpServletResponse;
  @InjectMocks private AuthController authController;

  @Test
  void logout_ShouldSetHeaderAndReturnOk_WhenRequestIsValid() {
    /* 1. Arrange */
    String jwtToken = "valid.jwt.token";
    JwtClaimDTO claimDTO =
        new JwtClaimDTO(
            UuidCreator.getTimeOrderedEpoch(),
            "user@example.com",
            UuidCreator.getTimeOrderedEpoch(),
            "role",
            "full name");
    /* Create a REAL ResponseCookie object to return from the mock */
    /* This ensures .toString() behaves exactly as Spring intends */
    ResponseCookie emptyCookie = ResponseCookie.from(COOKIE_NAME, "").maxAge(0).build();
    when(logoutUseCase.handle(claimDTO, jwtToken)).thenReturn(emptyCookie);
    /* 2. Act */
    ResponseEntity<MessageResponse> response =
        authController.logout(claimDTO, jwtToken, httpServletResponse);
    /* 3. Assert */
    assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertNotNull(response.getBody());
    /* Verify the response header was set with the cookie string */
    verify(httpServletResponse, times(1)).addHeader(HttpHeaders.SET_COOKIE, emptyCookie.toString());
  }

  @Test
  void logout_ShouldPropagateException_WhenUseCaseFails() {
    /* 1. Arrange */
    String jwtToken = "broken.token";
    JwtClaimDTO claimDTO =
        new JwtClaimDTO(
            UuidCreator.getTimeOrderedEpoch(),
            "user@example.com",
            UuidCreator.getTimeOrderedEpoch(),
            "role",
            "full name");
    /* Simulate a fatal error in the use case */
    when(logoutUseCase.handle(claimDTO, jwtToken)).thenThrow(new RuntimeException("Database down"));
    /* 2. Act & Assert */
    RuntimeException thrown =
        assertThrows(
            RuntimeException.class,
            () -> authController.logout(claimDTO, jwtToken, httpServletResponse));
    assertEquals("Database down", thrown.getMessage());
    /* Verify we never touched the response headers */
    verify(httpServletResponse, Mockito.never())
        .addHeader(Mockito.anyString(), Mockito.anyString());
  }
}
