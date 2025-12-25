package com.fw.irongate.web.api;

import static com.fw.irongate.constants.SystemConstants.COOKIE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.models.entities.Sysconfig;
import com.fw.irongate.models.entities.SysconfigType;
import com.fw.irongate.models.entities.User;
import com.fw.irongate.usecases.login.LoginRequest;
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
class TestApiAuthLogin extends TestParent {

  @Test
  void givenNullEmail_assert400() throws Exception {
    /* setup */
    LoginRequest request = new LoginRequest(null, "password");
    /* tests */
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void givenEmptyEmail_assert400() throws Exception {
    /* setup */
    LoginRequest request = new LoginRequest("", "password");
    /* tests */
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void givenBlankEmail_assert400() throws Exception {
    /* setup */
    LoginRequest request = new LoginRequest("  ", "password");
    /* tests */
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void givenNullPassword_assert400() throws Exception {
    /* setup */
    LoginRequest request = new LoginRequest("system@mail.com", null);
    /* tests */
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void givenEmptyPassword_assert400() throws Exception {
    /* setup */
    LoginRequest request = new LoginRequest("system@mail.com", "");
    /* tests */
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void givenBlankPassword_assert400() throws Exception {
    /* setup */
    LoginRequest request = new LoginRequest("system@mail.com", "  ");
    /* tests */
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void givenIncorrectPassword_assert403() throws Exception {
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
    LoginRequest request = new LoginRequest("am@mail.com", "asd");
    /* tests */
    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
    deleteAll();
  }

  @Test
  void givenValidCred_assert200() throws Exception {
    /* setup */
    SysconfigType role = createSysconfigType(sysconfigTypeRepository, "ROLE", "description");
    Sysconfig areaManager =
        createSysconfig(sysconfigRepository, role, "AREA_MANAGER", "Area Manager");
    User user =
        createUser(
            userRepository,
            areaManager,
            "am@mail.com",
            bCryptPasswordEncoder.encode("password"),
            "full name");
    LoginRequest request = new LoginRequest("am@mail.com", "password");
    /* tests */
    String cookie =
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
    JwtClaimDTO jwtClaimDTO = jwtUtil.validateJwt(cookie);
    assertEquals(user.getId().toString(), jwtClaimDTO.userId().toString());
    assertEquals(user.getEmail(), jwtClaimDTO.email());
    assertEquals(user.getRole().getId().toString(), jwtClaimDTO.roleId().toString());
    assertEquals(user.getRole().getValue(), jwtClaimDTO.roleName());
    assertEquals(user.getFullName(), jwtClaimDTO.fullName());
    deleteAll();
  }
}
