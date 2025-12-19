package com.fw.irongate.web.api;

import static com.fw.irongate.constants.SystemConstants.COOKIE_NAME;
import static com.fw.irongate.constants.SystemConstants.OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fw.irongate.usecases.login.LoginUseCase;
import com.fw.irongate.web.requests.LoginRequest;
import com.fw.irongate.web.responses.MessageResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class TestApiAuthLogin {

  @Mock private LoginUseCase loginUseCase;
  @Mock private HttpServletResponse httpServletResponse;
  @InjectMocks private AuthController authController;

  @Test
  void login_ShouldAddCookieToHeaderAndReturnOk_WhenRequestIsValid() {
    /* 1. Arrange */
    LoginRequest request = new LoginRequest("email", "password");
    /* Prepare the expected cookie string */
    String expectedCookieString = COOKIE_NAME + "=12345; Path=/; HttpOnly";
    /* Mock the object returned by the UseCase */
    /* We mock a generic Object to ensure we can control the .toString() output */
    /* If your UseCase returns a specific type (e.g. ResponseCookie), you can mock that type instead. */
    ResponseCookie cookie = mock(ResponseCookie.class);
    when(cookie.toString()).thenReturn(expectedCookieString);
    /* Mock the UseCase interaction */
    /* Note: We use 'Mockito.any()' or the specific 'request' object */
    when(loginUseCase.handle(request)).thenReturn(cookie);
    /* 2. Act */
    ResponseEntity<MessageResponse> response = authController.login(request, httpServletResponse);
    /* 3. Assert */
    /* Verify the status code is 200 OK */
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(OK, response.getBody().message());
    /* CRITICAL: Verify that the controller actually modified the HttpServletResponse */
    /* This ensures the cookie logic is actually executing */
    verify(httpServletResponse, times(1)).addHeader(HttpHeaders.SET_COOKIE, expectedCookieString);
  }

  @Test
  void login_ShouldPropagateException_WhenUseCaseFails() {
    /* 1. Arrange */
    LoginRequest request = new LoginRequest("email", "password");
    /* Simulate the UseCase throwing a runtime exception (e.g. Invalid Credentials) */
    /* specific exception class depends on your project, using RuntimeException here as placeholder */
    RuntimeException expectedException = new RuntimeException("Invalid Credentials");
    when(loginUseCase.handle(request)).thenThrow(expectedException);
    /* 2. Act & Assert */
    /* We expect the controller to simply let the exception bubble up */
    /* (to be caught by @ControllerAdvice in the real app) */
    RuntimeException thrown =
        assertThrows(
            RuntimeException.class,
            () -> {
              authController.login(request, httpServletResponse);
            });
    assertEquals("Invalid Credentials", thrown.getMessage());
    /* Verify we never tried to set the header since it crashed first */
    verify(httpServletResponse, never()).addHeader(anyString(), anyString());
  }
}
