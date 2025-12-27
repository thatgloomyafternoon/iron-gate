package com.fw.irongate.web.api;

import static com.fw.irongate.constants.SystemConstants.COOKIE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fw.irongate.models.dto.OrderDTO;
import com.fw.irongate.models.entities.Order;
import com.fw.irongate.models.entities.Product;
import com.fw.irongate.models.entities.Sysconfig;
import com.fw.irongate.models.entities.SysconfigType;
import com.fw.irongate.models.entities.User;
import com.fw.irongate.models.entities.Warehouse;
import com.fw.irongate.models.enums.OrderStatus;
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
class TestApiOrderFilter extends TestParent {

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
        .perform(get("/api/order/filter").cookie(cookieXx).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  void givenNoWarehouseMapping_assert400() throws Exception {
    mockMvc
        .perform(get("/api/order/filter").cookie(cookieYy).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void givenAreaManager_assert200_andAllOrdersFromAssignedWarehouses() throws Exception {
    String string =
        mockMvc
            .perform(
                get("/api/order/filter").cookie(cookieAm).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<OrderDTO> orders = objectMapper.readValue(string, new TypeReference<>() {});
    /* AM assigned to Hachioji (2 orders) and Tachikawa (1 order). Should see 3 orders. */
    assertEquals(3, orders.totalItems());
  }

  @Test
  void givenWarehouseManager_assert200_andOrdersFromOwnWarehouse() throws Exception {
    String string =
        mockMvc
            .perform(
                get("/api/order/filter").cookie(cookieWm).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<OrderDTO> orders = objectMapper.readValue(string, new TypeReference<>() {});
    /* WM assigned to Hachioji. Should see 2 orders. */
    assertEquals(2, orders.totalItems());
    assertEquals("Hachioji", orders.data().getFirst().warehouse());
  }

  @Test
  void givenDriver_assert403() throws Exception {
    /* Drivers cannot view Orders menu per requirements */
    mockMvc
        .perform(get("/api/order/filter").cookie(cookieWd).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  void givenFilterByCustomerName_assert200() throws Exception {
    String string =
        mockMvc
            .perform(
                get("/api/order/filter")
                    .param("customerName", "Customer 1")
                    .cookie(cookieAm)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<OrderDTO> orders = objectMapper.readValue(string, new TypeReference<>() {});
    assertEquals(1, orders.totalItems());
    assertEquals("Customer 1", orders.data().getFirst().customerName());
  }

  @Test
  void givenFilterByToWarehouse_assert200() throws Exception {
    String string =
        mockMvc
            .perform(
                get("/api/order/filter")
                    .param("toWarehouse", "Hachioji")
                    .cookie(cookieAm)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<OrderDTO> orders = objectMapper.readValue(string, new TypeReference<>() {});
    assertEquals(2, orders.totalItems());
    assertEquals("Hachioji", orders.data().getFirst().warehouse());
  }

  @Test
  void givenFilterByProductName_assert200() throws Exception {
    String string =
        mockMvc
            .perform(
                get("/api/order/filter")
                    .param("productName", "Product 1")
                    .cookie(cookieAm)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<OrderDTO> orders = objectMapper.readValue(string, new TypeReference<>() {});
    /* Product 1 is in Order 1 (Hachioji) and Order 3 (Tachikawa). */
    assertEquals(2, orders.totalItems());
  }

  @Test
  void givenFilterByMinTotalPrice_assert200() throws Exception {
    String string =
        mockMvc
            .perform(
                get("/api/order/filter")
                    .param("minTotalPrice", "55.00")
                    .cookie(cookieAm)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<OrderDTO> orders = objectMapper.readValue(string, new TypeReference<>() {});
    /* Order 2 is 60.00. */
    assertEquals(1, orders.totalItems());
    assertEquals("Customer 2", orders.data().getFirst().customerName());
  }

  @Test
  void givenFilterByMaxTotalPrice_assert200() throws Exception {
    String string =
        mockMvc
            .perform(
                get("/api/order/filter")
                    .param("maxTotalPrice", "55.00")
                    .cookie(cookieAm)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<OrderDTO> orders = objectMapper.readValue(string, new TypeReference<>() {});
    /* Order 1 is 50.00, Order 3 is 20.00. */
    assertEquals(2, orders.totalItems());
  }

  @Test
  void givenFilterByStatus_assert200() throws Exception {
    String string =
        mockMvc
            .perform(
                get("/api/order/filter")
                    .param("status", "COMPLETED")
                    .cookie(cookieAm)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<OrderDTO> orders = objectMapper.readValue(string, new TypeReference<>() {});
    /* Order 2 is COMPLETED. */
    assertEquals(1, orders.totalItems());
    assertEquals("Customer 2", orders.data().getFirst().customerName());
  }

  private void setup() throws Exception {
    SysconfigType r = createSysconfigType("ROLE", "desc");
    SysconfigType rp = createSysconfigType("RESOURCE_PATH", "desc");
    Sysconfig rAm = createSysconfig(r, "AREA_MANAGER", "Area Manager");
    Sysconfig rWm = createSysconfig(r, "WAREHOUSE_MANAGER", "Warehouse Manager");
    Sysconfig rWd = createSysconfig(r, "WAREHOUSE_DRIVER", "Warehouse Driver");
    Sysconfig rXx = createSysconfig(r, "XX", "XX");
    Sysconfig rYy = createSysconfig(r, "YY", "YY");
    Sysconfig rpAof = createSysconfig(rp, "API_ORDER_FILTER", "/api/order/filter");
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
    createPermission(rAm, rpAof);
    createPermission(rWm, rpAof);
    /* Driver does NOT have permission for Order Filter */
    createPermission(rYy, rpAof);
    cookieAm = login("am@mail.com", "password");
    cookieWm = login("wm@mail.com", "password");
    cookieWd = login("wd@mail.com", "password");
    cookieXx = login("xx@mail.com", "password");
    cookieYy = login("yy@mail.com", "password");
    Warehouse hachioji = createWarehouse("Hachioji", "HAC");
    Warehouse tachikawa = createWarehouse("Tachikawa", "TAC");
    Product product1 = createProduct("Product 1", "P-01", "desc", new BigDecimal("10.00"));
    Product product2 = createProduct("Product 2", "P-02", "desc", new BigDecimal("20.00"));
    /* Create Orders */
    /* Order 1: Hachioji, Customer 1, Product 1 (5 qty) -> 50.00, PENDING */
    Order order1 =
        createOrder(hachioji, "Customer 1", OrderStatus.PENDING.name(), new BigDecimal("50.00"));
    createOrderProduct(order1, product1, 5, new BigDecimal("10.00"));
    Thread.sleep(10); /* Ensure timestamp diff for sort order */
    /* Order 2: Hachioji, Customer 2, Product 2 (3 qty) -> 60.00, COMPLETED */
    Order order2 =
        createOrder(hachioji, "Customer 2", OrderStatus.COMPLETED.name(), new BigDecimal("60.00"));
    createOrderProduct(order2, product2, 3, new BigDecimal("20.00"));
    Thread.sleep(10);
    /* Order 3: Tachikawa, Customer 3, Product 1 (2 qty) -> 20.00, PENDING */
    Order order3 =
        createOrder(tachikawa, "Customer 3", OrderStatus.PENDING.name(), new BigDecimal("20.00"));
    createOrderProduct(order3, product1, 2, new BigDecimal("10.00"));
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
