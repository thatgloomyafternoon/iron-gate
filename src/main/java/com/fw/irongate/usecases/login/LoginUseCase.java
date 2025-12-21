package com.fw.irongate.usecases.login;

import com.fw.irongate.models.dto.SecurityUserDTO;
import com.fw.irongate.usecases.UseCase;
import com.fw.irongate.utils.CookieUtil;
import com.fw.irongate.utils.JwtUtil;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@UseCase
public class LoginUseCase {

  private final AuthenticationManager authenticationManager;
  private final CookieUtil cookieUtil;
  private final JwtUtil jwtUtil;

  public LoginUseCase(
      AuthenticationManager authenticationManager, CookieUtil cookieUtil, JwtUtil jwtUtil) {
    this.authenticationManager = authenticationManager;
    this.cookieUtil = cookieUtil;
    this.jwtUtil = jwtUtil;
  }

  public ResponseCookie handle(LoginRequest request) {
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password()));
    SecurityUserDTO securityUserDTO = (SecurityUserDTO) (authentication.getPrincipal());
    String jwt =
        jwtUtil.generateJwt(
            securityUserDTO.id().toString(),
            securityUserDTO.email(),
            securityUserDTO.roleId().toString(),
            securityUserDTO.roleName(),
            securityUserDTO.fullName());
    return cookieUtil.createFreshCookie(jwt);
  }
}
