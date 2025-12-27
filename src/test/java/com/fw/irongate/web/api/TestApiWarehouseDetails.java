package com.fw.irongate.web.api;

import static com.fw.irongate.constants.SystemConstants.COOKIE_NAME;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fw.irongate.models.entities.Sysconfig;
import com.fw.irongate.models.entities.SysconfigType;
import com.fw.irongate.models.entities.User;
import com.fw.irongate.models.entities.Warehouse;
import com.fw.irongate.usecases.login.LoginRequest;
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
class TestApiWarehouseDetails extends TestParent {

  private Cookie cookie;
  private Warehouse warehouse;

  @BeforeAll
  void beforeAll() throws Exception {
    setup();
  }

  @AfterAll
  void afterAll() {
    deleteAll();
  }

  @Test
  void givenId_assert200() throws Exception {
    mockMvc
        .perform(
            get("/api/warehouse/details")
                .param("id", warehouse.getId().toString())
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  private void setup() throws Exception {
    SysconfigType r = createSysconfigType("ROLE", "desc");
    SysconfigType rp = createSysconfigType("RESOURCE_PATH", "desc");
    Sysconfig rAm = createSysconfig(r, "AREA_MANAGER", "Area Manager");
    Sysconfig rpAwd = createSysconfig(rp, "API_WAREHOUSE_DETAILS", "/api/warehouse/details");
    createUser(rAm, "am@mail.com", bCryptPasswordEncoder.encode("password"), "full name");
    createPermission(rAm, rpAwd);
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
    warehouse = createWarehouse("Hachioji", "HAC");
    User manager = createUser(rAm, "manager@mail.com", "hash", "Manager Name");
    createWarehouseUser(warehouse, manager);
  }
}
