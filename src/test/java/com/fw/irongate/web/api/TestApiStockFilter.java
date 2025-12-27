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

@SuppressWarnings("SameParameterValue")
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource("classpath:application-test.properties")
class TestApiStockFilter extends TestParent {

  private Cookie cookieAm;
  private Cookie cookieWm;
  private Cookie cookieWd;
  private Cookie cookieXx;
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
    mockMvc
        .perform(get("/api/stock/filter").cookie(cookieXx).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  void givenNoWarehouseMapping_assert400() throws Exception {
    mockMvc
        .perform(get("/api/stock/filter").cookie(cookieYy).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void givenAreaManager_assert200_andAllStocksFromAssignedWarehouses() throws Exception {
    String string =
        mockMvc
            .perform(
                get("/api/stock/filter").cookie(cookieAm).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<StockDTO> stocks = objectMapper.readValue(string, new TypeReference<>() {});
    /* AM assigned to Hachioji (2 products) and Tachikawa (2 products). Should see 4 stocks. */
    assertEquals(4, stocks.totalItems());
  }

  @Test
  void givenWarehouseManager_assert200_andStocksFromOwnWarehouse() throws Exception {
    String string =
        mockMvc
            .perform(
                get("/api/stock/filter").cookie(cookieWm).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<StockDTO> stocks = objectMapper.readValue(string, new TypeReference<>() {});
    /* WM assigned to Hachioji. Should see 2 stocks. */
    assertEquals(2, stocks.totalItems());
    assertEquals("Hachioji", stocks.data().getFirst().warehouse());
  }

  @Test
  void givenDriver_assert200_andStocksFromRegisteredWarehouse() throws Exception {
    String string =
        mockMvc
            .perform(
                get("/api/stock/filter").cookie(cookieWd).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<StockDTO> stocks = objectMapper.readValue(string, new TypeReference<>() {});
    /* Driver assigned to Tachikawa. Should see 2 stocks. */
    assertEquals(2, stocks.totalItems());
    assertEquals("Tachikawa", stocks.data().getFirst().warehouse());
  }

  @Test
  void givenFilterByWarehouseName_assert200() throws Exception {
    String string =
        mockMvc
            .perform(
                get("/api/stock/filter")
                    .param("warehouseName", "Hachioji")
                    .cookie(cookieAm)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<StockDTO> stocks = objectMapper.readValue(string, new TypeReference<>() {});
    /* Should see 2 stocks from Hachioji */
    assertEquals(2, stocks.totalItems());
    assertEquals("Hachioji", stocks.data().getFirst().warehouse());
  }

  @Test
  void givenFilterByProductName_assert200() throws Exception {
    String string =
        mockMvc
            .perform(
                get("/api/stock/filter")
                    .param("productName", "Product 1")
                    .cookie(cookieAm)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<StockDTO> stocks = objectMapper.readValue(string, new TypeReference<>() {});
    /* Product 1 exists in both warehouses. */
    assertEquals(2, stocks.totalItems());
    assertEquals("Product 1", stocks.data().getFirst().productName());
  }

  @Test
  void givenFilterByMaxQuantity_assert200() throws Exception {
    String string =
        mockMvc
            .perform(
                get("/api/stock/filter")
                    .param("maxQuantity", "150")
                    .cookie(cookieAm)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<StockDTO> stocks = objectMapper.readValue(string, new TypeReference<>() {});
    /* Hachioji: P1(100), P2(200). Tachikawa: P1(300), P2(400).
    Max 150 -> Only Hachioji P1 (100). */
    assertEquals(1, stocks.totalItems());
    assertEquals("Product 1", stocks.data().getFirst().productName());
    assertEquals("Hachioji", stocks.data().getFirst().warehouse());
  }

  @Test
  void givenFilterByProductNameAndMaxQuantity_assert200() throws Exception {
    String string =
        mockMvc
            .perform(
                get("/api/stock/filter")
                    .param("productName", "Product 1")
                    .param("maxQuantity", "150")
                    .cookie(cookieAm)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<StockDTO> stocks = objectMapper.readValue(string, new TypeReference<>() {});
    /* Hachioji P1(100) - Match
    Tachikawa P1(300) - No Match (>150)
    Others - No Match (Product name) */
    assertEquals(1, stocks.totalItems());
    assertEquals("Product 1", stocks.data().getFirst().productName());
    assertEquals("Hachioji", stocks.data().getFirst().warehouse());
  }

  private void setup() throws Exception {
    SysconfigType r = createSysconfigType("ROLE", "desc");
    SysconfigType rp = createSysconfigType("RESOURCE_PATH", "desc");
    Sysconfig rAm = createSysconfig(r, "AREA_MANAGER", "Area Manager");
    Sysconfig rWm = createSysconfig(r, "WAREHOUSE_MANAGER", "Warehouse Manager");
    Sysconfig rWd = createSysconfig(r, "WAREHOUSE_DRIVER", "Warehouse Driver");
    Sysconfig rXx = createSysconfig(r, "XX", "XX");
    Sysconfig rYy = createSysconfig(r, "YY", "YY");
    Sysconfig rpAsf = createSysconfig(rp, "API_STOCK_FILTER", "/api/stock/filter");
    User uAm =
        createUser(rAm, "am@mail.com", bCryptPasswordEncoder.encode("password"), "Area Manager");
    User uWm =
        createUser(
            rWm, "wm@mail.com", bCryptPasswordEncoder.encode("password"), "Warehouse Manager");
    User uWd =
        createUser(
            rWd, "wd@mail.com", bCryptPasswordEncoder.encode("password"), "Warehouse Driver");
    createUser(rXx, "xx@mail.com", bCryptPasswordEncoder.encode("password"), "NoPerm");
    createUser(rYy, "yy@mail.com", bCryptPasswordEncoder.encode("password"), "NoWh");
    createPermission(rAm, rpAsf);
    createPermission(rWm, rpAsf);
    createPermission(rWd, rpAsf);
    createPermission(rYy, rpAsf);
    cookieAm = login("am@mail.com", "password");
    cookieWm = login("wm@mail.com", "password");
    cookieWd = login("wd@mail.com", "password");
    cookieXx = login("xx@mail.com", "password");
    cookieYy = login("yy@mail.com", "password");
    Warehouse hachioji = createWarehouse("Hachioji", "HAC");
    Warehouse tachikawa = createWarehouse("Tachikawa", "TAC");
    Product product1 = createProduct("Product 1", "P-01", "desc", new BigDecimal("10.00"));
    Product product2 = createProduct("Product 2", "P-02", "desc", new BigDecimal("20.00"));
    createStock(hachioji, product1, 100, 0);
    createStock(hachioji, product2, 200, 0);
    createStock(tachikawa, product1, 300, 0);
    createStock(tachikawa, product2, 400, 0);
    createWarehouseUser(hachioji, uAm);
    createWarehouseUser(tachikawa, uAm);
    createWarehouseUser(hachioji, uWm);
    createWarehouseUser(tachikawa, uWd);
  }

  private Cookie login(String email, String password) throws Exception {
    LoginRequest request = new LoginRequest(email, password);
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
    return new Cookie(COOKIE_NAME, cookieValue);
  }
}
