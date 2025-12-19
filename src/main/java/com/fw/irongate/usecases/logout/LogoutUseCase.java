package com.fw.irongate.usecases.logout;

import static com.fw.irongate.constants.MessageConstants.USER_ALREADY_LOGGED_OUT;

import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.models.entities.RevokedToken;
import com.fw.irongate.repositories.RevokedTokenRepository;
import com.fw.irongate.usecases.UseCase;
import com.fw.irongate.utils.CookieUtil;
import com.fw.irongate.utils.JwtUtil;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseCookie;

@UseCase
public class LogoutUseCase {

  private static final Logger log = LoggerFactory.getLogger(LogoutUseCase.class);
  private final RevokedTokenRepository revokedTokenRepository;
  private final JwtUtil jwtUtil;
  private final CookieUtil cookieUtil;

  public LogoutUseCase(
      RevokedTokenRepository revokedTokenRepository, JwtUtil jwtUtil, CookieUtil cookieUtil) {
    this.revokedTokenRepository = revokedTokenRepository;
    this.jwtUtil = jwtUtil;
    this.cookieUtil = cookieUtil;
  }

  public ResponseCookie handle(JwtClaimDTO jwtClaimDTO, String jwt) {
    try {
      Instant expiredAt = jwtUtil.extractExpiration(jwt);
      RevokedToken revokedToken = new RevokedToken(jwt, expiredAt);
      revokedToken.setCreatedBy(jwtClaimDTO.email());
      revokedToken.setUpdatedBy(jwtClaimDTO.email());
      revokedTokenRepository.save(revokedToken);
    } catch (Exception e) {
      log.warn(USER_ALREADY_LOGGED_OUT, jwtClaimDTO.email());
    }
    return cookieUtil.createEmptyCookie();
  }
}
