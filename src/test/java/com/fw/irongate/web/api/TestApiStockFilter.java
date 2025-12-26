package com.fw.irongate.web.api;

import static com.fw.irongate.constants.SystemConstants.COOKIE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fw.irongate.models.dto.StockDTO;
import com.fw.irongate.models.entities.Product;
import com.fw.irongate.models.entities.Sysconfig;
import com.fw.irongate.models.entities.SysconfigType;
import com.fw.irongate.models.entities.User;
import com.fw.irongate.models.entities.Warehouse;
import com.fw.irongate.usecases.login.LoginRequest;
import com.fw.irongate.web.responses.PaginatedResponse;
import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
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
class TestApiStockFilter extends TestParent {

  private Cookie cookieAm;
  private Cookie cookieXx;
  private Cookie cookieWm;
  private Cookie cookieYy;

  @BeforeAll
  void beforeAll() throws Exception {
    setup();
  }

  @AfterAll
  void afterAll() {
    deleteAll();
  }

  @Test
  void givenNoPermission_assert403() throws Exception {
    /* test */
    mockMvc
        .perform(get("/api/stock/filter").cookie(cookieXx).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  void givenNoWarehouseMapping_assert400() throws Exception {
    /* test */
    mockMvc
        .perform(get("/api/stock/filter").cookie(cookieYy).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void givenOneWarehouseMapping_assert200_andCorrectReturnData() throws Exception {
    /* test */
    String string =
        mockMvc
            .perform(
                get("/api/stock/filter").cookie(cookieWm).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<StockDTO> stocks = objectMapper.readValue(string, new TypeReference<>() {});
    assertEquals(2, stocks.totalItems());
    assertEquals("Hachioji", stocks.data().getFirst().warehouse());
    assertEquals("Product 2", stocks.data().getFirst().productName());
    assertEquals(11, stocks.data().getFirst().quantity());
    assertEquals("Hachioji", stocks.data().get(1).warehouse());
    assertEquals("Product 1", stocks.data().get(1).productName());
    assertEquals(10, stocks.data().get(1).quantity());
  }

  @Test
  void givenOneWarehouseMappingAndProductName_assert200_andCorrectReturnData() throws Exception {
    /* test */
    String string =
        mockMvc
            .perform(
                get("/api/stock/filter")
                    .param("query", "product 1")
                    .cookie(cookieWm)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<StockDTO> stocks = objectMapper.readValue(string, new TypeReference<>() {});
    assertEquals(1, stocks.totalItems());
    assertEquals("Hachioji", stocks.data().getFirst().warehouse());
    assertEquals("Product 1", stocks.data().getFirst().productName());
    assertEquals(10, stocks.data().getFirst().quantity());
  }

  @Test
  void givenAllWarehouseMappingsAndSkuAndMaxQty_assert200_andCorrectReturnData() throws Exception {
    /* test */
    String string =
        mockMvc
            .perform(
                get("/api/stock/filter")
                    .param("query", "tech")
                    .param("maxQuantity", "13")
                    .cookie(cookieAm)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<StockDTO> stocks = objectMapper.readValue(string, new TypeReference<>() {});
    assertEquals(2, stocks.totalItems());
    assertEquals("Tachikawa", stocks.data().getFirst().warehouse());
    assertEquals("Product 4", stocks.data().getFirst().productName());
    assertEquals(13, stocks.data().getFirst().quantity());
    assertEquals("Hachioji", stocks.data().get(1).warehouse());
    assertEquals("Product 2", stocks.data().get(1).productName());
    assertEquals(11, stocks.data().get(1).quantity());
  }

  private void setup() throws Exception {
    SysconfigType r = createSysconfigType("ROLE", "desc");
    SysconfigType rp = createSysconfigType("RESOURCE_PATH", "desc");
    Sysconfig rAm = createSysconfig(r, "AREA_MANAGER", "Area Manager");
    Sysconfig rWm = createSysconfig(r, "WAREHOUSE_MANAGER", "Warehouse Manager");
    Sysconfig rXx = createSysconfig(r, "XX", "XX");
    Sysconfig rYy = createSysconfig(r, "YY", "YY");
    Sysconfig rpAsf = createSysconfig(rp, "API_STOCK_FILTER", "/api/stock/filter");
    User uAm =
        createUser(rAm, "am@mail.com", bCryptPasswordEncoder.encode("password"), "full name");
    User uWm =
        createUser(rWm, "wm@mail.com", bCryptPasswordEncoder.encode("password"), "full name");
    createUser(rXx, "xx@mail.com", bCryptPasswordEncoder.encode("password"), "full name");
    createUser(rYy, "yy@mail.com", bCryptPasswordEncoder.encode("password"), "full name");
    createPermission(rAm, rpAsf);
    createPermission(rWm, rpAsf);
    createPermission(rYy, rpAsf);
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
    cookieAm = new Cookie(COOKIE_NAME, cookieValue);
    request = new LoginRequest("wm@mail.com", "password");
    cookieValue =
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
    cookieWm = new Cookie(COOKIE_NAME, cookieValue);
    request = new LoginRequest("xx@mail.com", "password");
    cookieValue =
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
    cookieXx = new Cookie(COOKIE_NAME, cookieValue);
    request = new LoginRequest("yy@mail.com", "password");
    cookieValue =
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
    cookieYy = new Cookie(COOKIE_NAME, cookieValue);
    Warehouse hachioji = createWarehouse("Hachioji", "HAC");
    Warehouse tachikawa = createWarehouse("Tachikawa", "TAC");
    Product product1 = createProduct("Product 1", "ACC-01", "asd", new BigDecimal("10.10"));
    Product product2 = createProduct("Product 2", "TECH-01", "asd", new BigDecimal("11.11"));
    Product product3 = createProduct("Product 3", "ACC-02", "asd", new BigDecimal("12.12"));
    Product product4 = createProduct("Product 4", "TECH-02", "asd", new BigDecimal("13.13"));
    createStock(hachioji, product1, 10, 0);
    createStock(hachioji, product2, 11, 0);
    createStock(tachikawa, product3, 12, 0);
    createStock(tachikawa, product4, 13, 0);
    createWarehouseUser(hachioji, uWm);
    createWarehouseUser(hachioji, uAm);
    createWarehouseUser(tachikawa, uAm);
  }
}
