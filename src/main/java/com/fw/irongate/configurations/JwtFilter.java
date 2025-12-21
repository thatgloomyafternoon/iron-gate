package com.fw.irongate.configurations;

import static com.fw.irongate.constants.MessageConstants.USER_ALREADY_LOGGED_OUT;
import static com.fw.irongate.constants.SystemConstants.COOKIE_NAME;
import static com.fw.irongate.constants.SystemConstants.JSON_INVALID_ROLE;
import static com.fw.irongate.constants.SystemConstants.JSON_NO_PERMISSION;
import static com.fw.irongate.constants.SystemConstants.JSON_UNAUTHORIZED;
import static com.fw.irongate.constants.SystemConstants.SYSTEM;

import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.models.entities.Sysconfig;
import com.fw.irongate.repositories.PermissionRepository;
import com.fw.irongate.repositories.RevokedTokenRepository;
import com.fw.irongate.repositories.SysconfigRepository;
import com.fw.irongate.utils.CookieUtil;
import com.fw.irongate.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);
  private final JwtUtil jwtUtil;
  private final CookieUtil cookieUtil;
  private final SysconfigRepository sysconfigRepository;
  private final PermissionRepository permissionRepository;
  private final RevokedTokenRepository revokedTokenRepository;

  public JwtFilter(
      JwtUtil jwtUtil,
      CookieUtil cookieUtil,
      SysconfigRepository sysconfigRepository,
      PermissionRepository permissionRepository,
      RevokedTokenRepository revokedTokenRepository) {
    this.jwtUtil = jwtUtil;
    this.cookieUtil = cookieUtil;
    this.sysconfigRepository = sysconfigRepository;
    this.permissionRepository = permissionRepository;
    this.revokedTokenRepository = revokedTokenRepository;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws IOException, ServletException {
    String uri = request.getRequestURI();
    if (request.getCookies() != null) {
      String jwt =
          Arrays.stream(request.getCookies())
              .filter(cookie -> cookie.getName().equals(COOKIE_NAME))
              .map(Cookie::getValue)
              .findFirst()
              .orElse(null);
      if (jwt != null && !jwt.isBlank()) {
        JwtClaimDTO jwtClaimDTO = jwtUtil.validateJwt(jwt);
        if (jwtClaimDTO == null) {
          setResponseStatusAndJson(
              response, HttpServletResponse.SC_UNAUTHORIZED, JSON_UNAUTHORIZED);
          return;
        }
        if (revokedTokenRepository.existsByJwt(jwt)) {
          ResponseCookie cookie = cookieUtil.createEmptyCookie();
          response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
          setResponseStatusAndJson(
              response, HttpServletResponse.SC_UNAUTHORIZED, JSON_UNAUTHORIZED);
          log.warn(USER_ALREADY_LOGGED_OUT, jwtClaimDTO.email());
          return;
        }
        Optional<Sysconfig> optSysconfig =
            sysconfigRepository.findOneActiveById(jwtClaimDTO.roleId());
        if (optSysconfig.isEmpty()) {
          setResponseStatusAndJson(response, HttpServletResponse.SC_BAD_REQUEST, JSON_INVALID_ROLE);
          return;
        } else if (!optSysconfig.get().getKey().equals(SYSTEM)
            && permissionRepository
                .findAllActiveByRoleIdAndResourcePath(jwtClaimDTO.roleId(), uri)
                .isEmpty()) {
          setResponseStatusAndJson(response, HttpServletResponse.SC_FORBIDDEN, JSON_NO_PERMISSION);
          return;
        }
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
          UsernamePasswordAuthenticationToken authToken =
              new UsernamePasswordAuthenticationToken(jwtClaimDTO, null, Collections.emptyList());
          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }
    }
    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.equals("/")
        || path.equals("/index.html")
        || path.startsWith("/assets/")
        || path.startsWith("/static/")
        || path.equals("/api/auth/login");
  }

  private void setResponseStatusAndJson(HttpServletResponse response, int status, String json)
      throws IOException {
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().write(json);
  }
}
