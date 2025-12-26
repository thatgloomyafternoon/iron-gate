package com.fw.irongate.web.api;

import static com.fw.irongate.constants.SystemConstants.COOKIE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fw.irongate.models.entities.Sysconfig;
import com.fw.irongate.models.entities.SysconfigType;
import com.fw.irongate.usecases.login.LoginRequest;
import jakarta.servlet.http.Cookie;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource("classpath:application-test.properties")
class TestApiAuthLogout extends TestParent {

  @Test
  void givenNoCookie_assert403() throws Exception {
    /* test */
    mockMvc
        .perform(post("/api/auth/logout").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  void givenIncorrectCookieName_assert403() throws Exception {
    /* setup */
    Cookie cookie = new Cookie("cookie", "asdfghjkl");
    /* test */
    mockMvc
        .perform(post("/api/auth/logout").cookie(cookie).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  void givenNoPermission_assert403() throws Exception {
    /* setup */
    SysconfigType role = createSysconfigType(sysconfigTypeRepository, "ROLE", "description");
    Sysconfig areaManager =
        createSysconfig(sysconfigRepository, role, "AREA_MANAGER", "Area Manager");
    createUser(
        userRepository,
        areaManager,
        "am@mail.com",
        bCryptPasswordEncoder.encode("password"),
        "full name");
    LoginRequest request = new LoginRequest("am@mail.com", "password");
    String cookieValue =
        Objects.requireNonNull(
                mockMvc
                    .perform(
                        post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(cookie().exists(COOKIE_NAME))
                    .andReturn()
                    .getResponse()
                    .getCookie(COOKIE_NAME))
            .getValue();
    Cookie cookie = new Cookie(COOKIE_NAME, cookieValue);
    /* test */
    mockMvc
        .perform(post("/api/auth/logout").cookie(cookie).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
    deleteAll();
  }

  @Test
  void givenCookieAndPermission_assert200_andReturnNoCookie() throws Exception {
    /* setup */
    SysconfigType role = createSysconfigType(sysconfigTypeRepository, "ROLE", "description");
    SysconfigType resourcePath =
        createSysconfigType(sysconfigTypeRepository, "RESOURCE_PATH", "description");
    Sysconfig areaManager =
        createSysconfig(sysconfigRepository, role, "AREA_MANAGER", "Area Manager");
    Sysconfig apiAuthLogout =
        createSysconfig(sysconfigRepository, resourcePath, "API_AUTH_LOGOUT", "/api/auth/logout");
    createUser(
        userRepository,
        areaManager,
        "am@mail.com",
        bCryptPasswordEncoder.encode("password"),
        "full name");
    createPermission(permissionRepository, areaManager, apiAuthLogout);
    LoginRequest request = new LoginRequest("am@mail.com", "password");
    String cookieValue1 =
        Objects.requireNonNull(
                mockMvc
                    .perform(
                        post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(cookie().exists(COOKIE_NAME))
                    .andReturn()
                    .getResponse()
                    .getCookie(COOKIE_NAME))
            .getValue();
    Cookie cookie = new Cookie(COOKIE_NAME, cookieValue1);
    /* test */
    String cookieValue2 =
        Objects.requireNonNull(
                mockMvc
                    .perform(
                        post("/api/auth/logout")
                            .cookie(cookie)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(cookie().exists(COOKIE_NAME))
                    .andReturn()
                    .getResponse()
                    .getCookie(COOKIE_NAME))
            .getValue();
    assertEquals("", cookieValue2);
    assertEquals(1, revokedTokenRepository.findAll().size());
    assertEquals(cookieValue1, revokedTokenRepository.findAll().getFirst().getJwt());
    deleteAll();
  }

  @Test
  void givenAlreadyLoggedOut_assert401() throws Exception {
    /* setup */
    SysconfigType role = createSysconfigType(sysconfigTypeRepository, "ROLE", "description");
    SysconfigType resourcePath =
        createSysconfigType(sysconfigTypeRepository, "RESOURCE_PATH", "description");
    Sysconfig areaManager =
        createSysconfig(sysconfigRepository, role, "AREA_MANAGER", "Area Manager");
    Sysconfig apiAuthLogout =
        createSysconfig(sysconfigRepository, resourcePath, "API_AUTH_LOGOUT", "/api/auth/logout");
    createUser(
        userRepository,
        areaManager,
        "am@mail.com",
        bCryptPasswordEncoder.encode("password"),
        "full name");
    createPermission(permissionRepository, areaManager, apiAuthLogout);
    LoginRequest request = new LoginRequest("am@mail.com", "password");
    String cookieValue1 =
        Objects.requireNonNull(
                mockMvc
                    .perform(
                        post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(cookie().exists(COOKIE_NAME))
                    .andReturn()
                    .getResponse()
                    .getCookie(COOKIE_NAME))
            .getValue();
    Cookie cookie = new Cookie(COOKIE_NAME, cookieValue1);
    /* test */
    mockMvc
        .perform(post("/api/auth/logout").cookie(cookie).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
    mockMvc
        .perform(post("/api/auth/logout").cookie(cookie).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
    deleteAll();
  }
}
