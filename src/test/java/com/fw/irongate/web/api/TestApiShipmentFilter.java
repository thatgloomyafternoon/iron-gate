package com.fw.irongate.web.api;

import static com.fw.irongate.constants.SystemConstants.COOKIE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fw.irongate.models.dto.ShipmentDTO;
import com.fw.irongate.models.entities.Product;
import com.fw.irongate.models.entities.Stock;
import com.fw.irongate.models.entities.Sysconfig;
import com.fw.irongate.models.entities.SysconfigType;
import com.fw.irongate.models.entities.User;
import com.fw.irongate.models.entities.Warehouse;
import com.fw.irongate.models.enums.ShipmentStatus;
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
class TestApiShipmentFilter extends TestParent {

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
        .perform(
            get("/api/shipment/filter").cookie(cookieXx).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  void givenNoWarehouseMapping_assert400() throws Exception {
    mockMvc
        .perform(
            get("/api/shipment/filter").cookie(cookieYy).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void givenDirectorOfLogistics_assert200_andAllShipmentsFromAssignedWarehouses() throws Exception {
    String string =
        mockMvc
            .perform(
                get("/api/shipment/filter")
                    .cookie(cookieAm)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<ShipmentDTO> shipments =
        objectMapper.readValue(string, new TypeReference<>() {});
    /* AM assigned to Hachioji and Tachikawa. Should see S1, S2, S3. */
    assertEquals(3, shipments.totalItems());
  }

  @Test
  void givenWarehouseManager_assert200_andShipmentsFromOwnWarehouse() throws Exception {
    String string =
        mockMvc
            .perform(
                get("/api/shipment/filter")
                    .cookie(cookieWm)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<ShipmentDTO> shipments =
        objectMapper.readValue(string, new TypeReference<>() {});
    /* WM assigned to Hachioji. Should see S1, S2. */
    assertEquals(2, shipments.totalItems());
  }

  @Test
  void givenDriver_assert200_andShipmentsFromRegisteredWarehouse() throws Exception {
    String string =
        mockMvc
            .perform(
                get("/api/shipment/filter")
                    .cookie(cookieWd)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<ShipmentDTO> shipments =
        objectMapper.readValue(string, new TypeReference<>() {});
    /* Driver assigned to Tachikawa. Should see S3. */
    assertEquals(1, shipments.totalItems());
    assertEquals("S3", shipments.data().getFirst().code());
  }

  @Test
  void givenFilterByCode_assert200() throws Exception {
    String string =
        mockMvc
            .perform(
                get("/api/shipment/filter")
                    .param("code", "S2")
                    .cookie(cookieAm)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<ShipmentDTO> shipments =
        objectMapper.readValue(string, new TypeReference<>() {});
    assertEquals(1, shipments.totalItems());
    assertEquals("S2", shipments.data().getFirst().code());
  }

  @Test
  void givenFilterByProductName_assert200() throws Exception {
    String string =
        mockMvc
            .perform(
                get("/api/shipment/filter")
                    .param("productName", "Product 2")
                    .cookie(cookieAm)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<ShipmentDTO> shipments =
        objectMapper.readValue(string, new TypeReference<>() {});
    assertEquals(1, shipments.totalItems());
    assertEquals("S2", shipments.data().getFirst().code());
  }

  @Test
  void givenFilterByQuantity_assert200() throws Exception {
    String string =
        mockMvc
            .perform(
                get("/api/shipment/filter")
                    .param("minQuantity", "15")
                    .param("maxQuantity", "25")
                    .cookie(cookieAm)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<ShipmentDTO> shipments =
        objectMapper.readValue(string, new TypeReference<>() {});
    /* S2 has 20. */
    assertEquals(1, shipments.totalItems());
    assertEquals("S2", shipments.data().getFirst().code());
  }

  @Test
  void givenFilterByFromWarehouse_assert200() throws Exception {
    String string =
        mockMvc
            .perform(
                get("/api/shipment/filter")
                    .param("from", "Tachikawa")
                    .cookie(cookieAm)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<ShipmentDTO> shipments =
        objectMapper.readValue(string, new TypeReference<>() {});
    /* S3 is from Tachikawa. */
    assertEquals(1, shipments.totalItems());
    assertEquals("S3", shipments.data().getFirst().code());
  }

  @Test
  void givenFilterByToWarehouse_assert200() throws Exception {
    String string =
        mockMvc
            .perform(
                get("/api/shipment/filter")
                    .param("to", "Shinjuku")
                    .cookie(cookieAm)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<ShipmentDTO> shipments =
        objectMapper.readValue(string, new TypeReference<>() {});
    /* All S1, S2, S3 to Shinjuku. */
    assertEquals(3, shipments.totalItems());
  }

  @Test
  void givenFilterByStatus_assert200() throws Exception {
    String string =
        mockMvc
            .perform(
                get("/api/shipment/filter")
                    .param("status", "DELIVERED")
                    .cookie(cookieAm)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<ShipmentDTO> shipments =
        objectMapper.readValue(string, new TypeReference<>() {});
    /* S2 is DELIVERED. */
    assertEquals(1, shipments.totalItems());
    assertEquals("S2", shipments.data().getFirst().code());
  }

  @Test
  void givenFilterByAssignedTo_assert200() throws Exception {
    String string =
        mockMvc
            .perform(
                get("/api/shipment/filter")
                    .param("assignedTo", "driver1")
                    .cookie(cookieAm)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<ShipmentDTO> shipments =
        objectMapper.readValue(string, new TypeReference<>() {});
    /* S1, S3 assigned to driver1. */
    assertEquals(2, shipments.totalItems());
  }

  private void setup() throws Exception {
    SysconfigType r = createSysconfigType("ROLE", "desc");
    SysconfigType rp = createSysconfigType("RESOURCE_PATH", "desc");
    Sysconfig rAm = createSysconfig(r, "AREA_MANAGER", "Area Manager");
    Sysconfig rWm = createSysconfig(r, "WAREHOUSE_MANAGER", "Warehouse Manager");
    Sysconfig rWd = createSysconfig(r, "WAREHOUSE_DRIVER", "Warehouse Driver");
    Sysconfig rXx = createSysconfig(r, "XX", "XX");
    Sysconfig rYy = createSysconfig(r, "YY", "YY");
    Sysconfig rpAsf = createSysconfig(rp, "API_SHIPMENT_FILTER", "/api/shipment/filter");
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
    Warehouse shinjuku = createWarehouse("Shinjuku", "SHI");
    Product product1 = createProduct("Product 1", "P-01", "desc", new BigDecimal("10.00"));
    Product product2 = createProduct("Product 2", "P-02", "desc", new BigDecimal("20.00"));
    Stock stockHacP1 = createStock(hachioji, product1, 100, 0);
    Stock stockHacP2 = createStock(hachioji, product2, 100, 0);
    Stock stockTacP1 = createStock(tachikawa, product1, 100, 0);
    createShipment(stockHacP1, shinjuku, 10, ShipmentStatus.PENDING.name(), "S1", "driver1");
    Thread.sleep(10); /* Ensure timestamp diff for sort order */
    createShipment(stockHacP2, shinjuku, 20, ShipmentStatus.DELIVERED.name(), "S2", "driver2");
    Thread.sleep(10);
    createShipment(stockTacP1, shinjuku, 30, ShipmentStatus.PENDING.name(), "S3", "driver1");
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
