package com.fw.irongate.web.api;

import static com.fw.irongate.constants.SystemConstants.COOKIE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fw.irongate.models.dto.WarehouseDTO;
import com.fw.irongate.models.entities.Sysconfig;
import com.fw.irongate.models.entities.SysconfigType;
import com.fw.irongate.usecases.login.LoginRequest;
import com.fw.irongate.web.responses.PaginatedResponse;
import jakarta.servlet.http.Cookie;
import java.util.Objects;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
class TestApiWarehouseFilter extends TestParent {

  private Cookie cookie;

  @BeforeAll
  void beforeAll() throws Exception {
    setup();
  }

  @AfterAll
  void afterAll() {
    deleteAll();
  }

  @Test
  void givenName_assert200_andCorrectReturnData() throws Exception {
    /* test */
    String string =
        mockMvc
            .perform(
                get("/api/warehouse/filter")
                    .param("query", "Hac")
                    .cookie(cookie)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<WarehouseDTO> warehouses =
        objectMapper.readValue(string, new TypeReference<>() {});
    assertEquals(1, warehouses.totalItems());
    assertEquals("Hachioji", warehouses.data().getFirst().name());
    assertEquals("HAC", warehouses.data().getFirst().code());
  }

  // givenCode_assert200_andCorrectReturnData
  @Test
  void givenCode_assert200_andCorrectReturnData() throws Exception {
    /* test */
    String string =
        mockMvc
            .perform(
                get("/api/warehouse/filter")
                    .param("query", "T")
                    .cookie(cookie)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<WarehouseDTO> warehouses =
        objectMapper.readValue(string, new TypeReference<>() {});
    assertEquals(2, warehouses.totalItems());
    assertEquals("Tokyo", warehouses.data().getFirst().name());
    assertEquals("TYO", warehouses.data().getFirst().code());
    assertEquals("Tachikawa", warehouses.data().get(1).name());
    assertEquals("TAC", warehouses.data().get(1).code());
  }

  private void setup() throws Exception {
    SysconfigType r = createSysconfigType("ROLE", "desc");
    SysconfigType rp = createSysconfigType("RESOURCE_PATH", "desc");
    Sysconfig rAm = createSysconfig(r, "AREA_MANAGER", "Area Manager");
    Sysconfig rpAwf = createSysconfig(rp, "API_WAREHOUSE_FILTER", "/api/warehouse/filter");
    createUser(rAm, "am@mail.com", bCryptPasswordEncoder.encode("password"), "full name");
    createPermission(rAm, rpAwf);
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
    cookie = new Cookie(COOKIE_NAME, cookieValue);
    createWarehouse("Hachioji", "HAC");
    createWarehouse("Tachikawa", "TAC");
    createWarehouse("Tokyo", "TYO");
  }
}
