package com.fw.irongate.configurations;

import static com.fw.irongate.constants.SystemConstants.COOKIE_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.models.entities.Sysconfig;
import com.fw.irongate.repositories.PermissionRepository;
import com.fw.irongate.repositories.RevokedTokenRepository;
import com.fw.irongate.repositories.SysconfigRepository;
import com.fw.irongate.utils.CookieUtil;
import com.fw.irongate.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestJwtFilter {

  private JwtUtil jwtUtil;
  private SysconfigRepository sysconfigRepository;
  private PermissionRepository permissionRepository;
  private RevokedTokenRepository revokedTokenRepository;
  private JwtFilter jwtFilter;

  @BeforeEach
  void setUp() {
    jwtUtil = mock(JwtUtil.class);
    CookieUtil cookieUtil = mock(CookieUtil.class);
    sysconfigRepository = mock(SysconfigRepository.class);
    permissionRepository = mock(PermissionRepository.class);
    revokedTokenRepository = mock(RevokedTokenRepository.class);
    jwtFilter =
        new JwtFilter(
            jwtUtil, cookieUtil, sysconfigRepository, permissionRepository, revokedTokenRepository);
  }

  @Test
  void doFilterInternal_shouldAllowFrontendPath_whenRoleIsNotSystem_andNoPermissionEntry()
      throws Exception {
    /* Arrange */
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain filterChain = mock(FilterChain.class);
    String path = "/dashboard";
    when(request.getRequestURI()).thenReturn(path);
    Cookie cookie = new Cookie(COOKIE_NAME, "valid.jwt.token");
    when(request.getCookies()).thenReturn(new Cookie[] {cookie});
    JwtClaimDTO claimDTO =
        new JwtClaimDTO(
            UUID.randomUUID(), "am@mail.com", UUID.randomUUID(), "Area Manager", "Full Name");
    when(jwtUtil.validateJwt("valid.jwt.token")).thenReturn(claimDTO);
    when(revokedTokenRepository.existsByJwt("valid.jwt.token")).thenReturn(false);
    Sysconfig roleConfig = new Sysconfig();
    roleConfig.setKey("AREA_MANAGER");
    when(sysconfigRepository.findOneActiveById(claimDTO.roleId()))
        .thenReturn(Optional.of(roleConfig));
    /* No permission for /dashboard */
    when(permissionRepository.findAllActiveByRoleIdAndResourcePath(claimDTO.roleId(), path))
        .thenReturn(Collections.emptyList());
    when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
    /* Act */
    jwtFilter.doFilterInternal(request, response, filterChain);
    /* Assert */
    /* We expect the filter chain to proceed for non-api paths even if no permission */
    verify(filterChain, times(1)).doFilter(request, response);
  }
}
