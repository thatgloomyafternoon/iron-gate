package com.fw.irongate.web.api;

import static com.fw.irongate.constants.SystemConstants.COOKIE_NAME;
import static com.fw.irongate.constants.SystemConstants.OK;

import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.models.dto.UserDTO;
import com.fw.irongate.usecases.login.LoginRequest;
import com.fw.irongate.usecases.login.LoginUseCase;
import com.fw.irongate.usecases.logout.LogoutUseCase;
import com.fw.irongate.web.responses.MessageResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final LoginUseCase loginUseCase;
  private final LogoutUseCase logoutUseCase;

  public AuthController(LoginUseCase loginUseCase, LogoutUseCase logoutUseCase) {
    this.loginUseCase = loginUseCase;
    this.logoutUseCase = logoutUseCase;
  }

  @PostMapping("/login")
  public ResponseEntity<MessageResponse> login(
      @RequestBody @Valid LoginRequest request, HttpServletResponse response) {
    response.addHeader(HttpHeaders.SET_COOKIE, loginUseCase.handle(request).toString());
    return ResponseEntity.ok(new MessageResponse(OK));
  }

  @PostMapping("/logout")
  public ResponseEntity<MessageResponse> logout(
      @AuthenticationPrincipal JwtClaimDTO jwtClaimDTO,
      @CookieValue(name = COOKIE_NAME) String jwt,
      HttpServletResponse response) {
    response.addHeader(HttpHeaders.SET_COOKIE, logoutUseCase.handle(jwtClaimDTO, jwt).toString());
    return ResponseEntity.ok(new MessageResponse((OK)));
  }

  /**
   * This will be hit from the login page, to confirm if there is an active user logged in or not.
   * The frontend will get 403 if there is no user logged in.<br>
   * This is standard behavior for Single Page Applications (SPAs) with session persistence.
   * The 403 is "expected" in the sense that it simply confirms "No, there is no active user."
   */
  @GetMapping("/me")
  public ResponseEntity<UserDTO> me(@AuthenticationPrincipal JwtClaimDTO jwtClaimDTO) {
    return ResponseEntity.ok(
        new UserDTO(
            jwtClaimDTO.userId(),
            jwtClaimDTO.email(),
            jwtClaimDTO.roleName(),
            jwtClaimDTO.fullName()));
  }
}
