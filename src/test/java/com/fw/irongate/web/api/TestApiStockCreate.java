package com.fw.irongate.web.api;

import static com.fw.irongate.constants.MessageConstants.OPERATION_NOT_PERMITTED;
import static com.fw.irongate.constants.MessageConstants.PRODUCT_NOT_FOUND;
import static com.fw.irongate.constants.SystemConstants.COOKIE_NAME;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fw.irongate.models.entities.Product;
import com.fw.irongate.models.entities.Stock;
import com.fw.irongate.models.entities.Sysconfig;
import com.fw.irongate.models.entities.SysconfigType;
import com.fw.irongate.models.entities.User;
import com.fw.irongate.models.entities.Warehouse;
import com.fw.irongate.usecases.create_stock.CreateStockRequest;
import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

@SuppressWarnings("FieldCanBeLocal")
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource("classpath:application-test.properties")
class TestApiStockCreate extends TestParent {

  private SysconfigType roleType;
  private SysconfigType resourcePathType;
  private Sysconfig roleReceivingClerk;
  private Sysconfig roleOther;
  private Sysconfig resourceCreateStock;
  private User receivingClerk;
  private User otherUser;
  private Warehouse warehouse;
  private Product product;
  private String receivingClerkJwt;
  private String otherUserJwt;

  @BeforeEach
  void setUp() {
    roleType = createSysconfigType("ROLE", "Role Type");
    resourcePathType = createSysconfigType("RESOURCE_PATH", "Resource Path Type");
    roleReceivingClerk = createSysconfig(roleType, "RECEIVING_CLERK", "Receiving Clerk");
    roleOther = createSysconfig(roleType, "OTHER_ROLE", "Other Role");
    resourceCreateStock = createSysconfig(resourcePathType, "STOCK_CREATE", "/api/stock/create");
    createPermission(roleReceivingClerk, resourceCreateStock);
    receivingClerk = createUser(roleReceivingClerk, "rc@example.com", "hash", "Receiving Clerk");
    otherUser = createUser(roleOther, "other@example.com", "hash", "Other User");
    warehouse = createWarehouse("Warehouse 1", "WH1");
    product = createProduct("Product 1", "SKU-001", "Desc", new BigDecimal("10.00"));
    /* Assign receiving clerk to warehouse */
    createWarehouseUser(warehouse, receivingClerk);
    receivingClerkJwt =
        jwtUtil.generateJwt(
            receivingClerk.getId().toString(),
            receivingClerk.getEmail(),
            receivingClerk.getRole().getId().toString(),
            receivingClerk.getRole().getValue(),
            receivingClerk.getFullName());
    otherUserJwt =
        jwtUtil.generateJwt(
            otherUser.getId().toString(),
            otherUser.getEmail(),
            otherUser.getRole().getId().toString(),
            otherUser.getRole().getValue(),
            otherUser.getFullName());
  }

  @AfterEach
  void tearDown() {
    deleteAll();
  }

  @Test
  void create_ShouldReturn200AndId_WhenNewStockCreated() throws Exception {
    CreateStockRequest request =
        new CreateStockRequest(warehouse.getId(), product.getId(), new BigInteger("100"));
    mockMvc
        .perform(
            post("/api/stock/create")
                .cookie(new Cookie(COOKIE_NAME, receivingClerkJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").isNotEmpty());
    assertTrue(
        stockRepository
            .findByWarehouseIdAndProductId(warehouse.getId(), product.getId())
            .isPresent());
    Stock stock =
        stockRepository.findByWarehouseIdAndProductId(warehouse.getId(), product.getId()).get();
    assertEquals(100, stock.getQuantity());
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  @Test
  void create_ShouldReturn200AndId_WhenUpdatingExistingStock() throws Exception {
    /* Arrange: Create existing stock */
    createStock(warehouse, product, 50, 0);
    CreateStockRequest request =
        new CreateStockRequest(warehouse.getId(), product.getId(), new BigInteger("200"));
    mockMvc
        .perform(
            post("/api/stock/create")
                .cookie(new Cookie(COOKIE_NAME, receivingClerkJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").isNotEmpty());
    Stock stock =
        stockRepository.findByWarehouseIdAndProductId(warehouse.getId(), product.getId()).get();
    assertEquals(200, stock.getQuantity());
  }

  @Test
  void create_ShouldReturn403_WhenUserUnauthorized() throws Exception {
    CreateStockRequest request =
        new CreateStockRequest(warehouse.getId(), product.getId(), new BigInteger("100"));
    mockMvc
        .perform(
            post("/api/stock/create")
                .cookie(new Cookie(COOKIE_NAME, otherUserJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error", containsString("No Permission")));
  }

  @Test
  void create_ShouldReturn400_WhenUserNotAssignedToWarehouse() throws Exception {
    /* Arrange: Create another warehouse not assigned to user */
    Warehouse otherWarehouse = createWarehouse("Warehouse 2", "WH2");
    CreateStockRequest request =
        new CreateStockRequest(otherWarehouse.getId(), product.getId(), new BigInteger("100"));
    mockMvc
        .perform(
            post("/api/stock/create")
                .cookie(new Cookie(COOKIE_NAME, receivingClerkJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", containsString(OPERATION_NOT_PERMITTED)));
  }

  @Test
  void create_ShouldReturn400_WhenProductNotFound() throws Exception {
    CreateStockRequest request =
        new CreateStockRequest(warehouse.getId(), UUID.randomUUID(), new BigInteger("100"));
    mockMvc
        .perform(
            post("/api/stock/create")
                .cookie(new Cookie(COOKIE_NAME, receivingClerkJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", containsString(PRODUCT_NOT_FOUND)));
  }

  @Test
  void create_ShouldReturn400_WhenWarehouseIdIsNull() throws Exception {
    CreateStockRequest request =
        new CreateStockRequest(null, product.getId(), new BigInteger("100"));
    mockMvc
        .perform(
            post("/api/stock/create")
                .cookie(new Cookie(COOKIE_NAME, receivingClerkJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", containsString("Warehouse ID cannot be null")));
  }

  @Test
  void create_ShouldReturn400_WhenProductIdIsNull() throws Exception {
    CreateStockRequest request =
        new CreateStockRequest(warehouse.getId(), null, new BigInteger("100"));
    mockMvc
        .perform(
            post("/api/stock/create")
                .cookie(new Cookie(COOKIE_NAME, receivingClerkJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", containsString("Product ID cannot be null")));
  }

  @Test
  void create_ShouldReturn400_WhenQuantityIsNull() throws Exception {
    CreateStockRequest request = new CreateStockRequest(warehouse.getId(), product.getId(), null);
    mockMvc
        .perform(
            post("/api/stock/create")
                .cookie(new Cookie(COOKIE_NAME, receivingClerkJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void create_ShouldReturn400_WhenQuantityIsNegative() throws Exception {
    CreateStockRequest request =
        new CreateStockRequest(warehouse.getId(), product.getId(), new BigInteger("-100"));
    mockMvc
        .perform(
            post("/api/stock/create")
                .cookie(new Cookie(COOKIE_NAME, receivingClerkJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", containsString("Quantity must be greater than 0")));
  }

  @Test
  void create_ShouldReturn400_WhenQuantityIsZero() throws Exception {
    CreateStockRequest request =
        new CreateStockRequest(warehouse.getId(), product.getId(), BigInteger.ZERO);
    mockMvc
        .perform(
            post("/api/stock/create")
                .cookie(new Cookie(COOKIE_NAME, receivingClerkJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", containsString("Quantity must be greater than 0")));
  }

  @Test
  void create_ShouldReturn400_WhenQuantityIsTooLarge() throws Exception {
    /* 10 digits */
    BigInteger largeQuantity = new BigInteger("1234567890");
    CreateStockRequest request =
        new CreateStockRequest(warehouse.getId(), product.getId(), largeQuantity);
    mockMvc
        .perform(
            post("/api/stock/create")
                .cookie(new Cookie(COOKIE_NAME, receivingClerkJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath(
                "$.error", containsString("Quantity is too large or has too many decimal places")));
  }
}
