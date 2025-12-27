package com.fw.irongate.web.api;

import static com.fw.irongate.constants.MessageConstants.DEST_WAREHOUSE_NOT_FOUND;
import static com.fw.irongate.constants.MessageConstants.INSUFFICIENT_STOCK;
import static com.fw.irongate.constants.MessageConstants.OPERATION_NOT_PERMITTED;
import static com.fw.irongate.constants.MessageConstants.STOCK_NOT_FOUND;
import static com.fw.irongate.constants.SystemConstants.COOKIE_NAME;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fw.irongate.models.entities.Counter;
import com.fw.irongate.models.entities.Product;
import com.fw.irongate.models.entities.Shipment;
import com.fw.irongate.models.entities.Stock;
import com.fw.irongate.models.entities.Sysconfig;
import com.fw.irongate.models.entities.SysconfigType;
import com.fw.irongate.models.entities.User;
import com.fw.irongate.models.entities.Warehouse;
import com.fw.irongate.repositories.CounterRepository;
import com.fw.irongate.usecases.create_shipment.CreateShipmentRequest;
import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

@SuppressWarnings("FieldCanBeLocal")
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource("classpath:application-test.properties")
class TestApiShipmentCreate extends TestParent {

  @Autowired private CounterRepository counterRepository;

  private SysconfigType roleType;
  private SysconfigType resourcePathType;
  private Sysconfig roleWarehouseManager;
  private Sysconfig resourceCreateShipment;
  private User warehouseManager;
  private User otherUser;
  private Warehouse originWarehouse;
  private Warehouse destWarehouse;
  private Product product;
  private Stock stock;
  private String warehouseManagerJwt;
  private String otherUserJwt;

  @BeforeEach
  void setUp() {
    roleType = createSysconfigType("ROLE", "Role Type");
    resourcePathType = createSysconfigType("RESOURCE_PATH", "Resource Path Type");
    roleWarehouseManager = createSysconfig(roleType, "WAREHOUSE_MANAGER", "Warehouse Manager");
    Sysconfig roleDriver = createSysconfig(roleType, "DRIVER", "Driver");
    resourceCreateShipment =
        createSysconfig(resourcePathType, "SHIPMENT_CREATE", "/api/shipment/create");
    createPermission(roleWarehouseManager, resourceCreateShipment);
    warehouseManager =
        createUser(roleWarehouseManager, "wm@example.com", "hash", "Warehouse Manager");
    otherUser = createUser(roleDriver, "driver@example.com", "hash", "Driver");
    originWarehouse = createWarehouse("Origin Warehouse", "ORG");
    destWarehouse = createWarehouse("Dest Warehouse", "DST");
    product = createProduct("Product 1", "SKU-001", "Desc", new BigDecimal("10.00"));
    /* Create stock: 100 quantity, 0 allocated */
    stock = createStock(originWarehouse, product, 100, 0);
    /* Assign warehouse manager to origin warehouse */
    createWarehouseUser(originWarehouse, warehouseManager);
    warehouseManagerJwt =
        jwtUtil.generateJwt(
            warehouseManager.getId().toString(),
            warehouseManager.getEmail(),
            warehouseManager.getRole().getId().toString(),
            warehouseManager.getRole().getValue(),
            warehouseManager.getFullName());
    otherUserJwt =
        jwtUtil.generateJwt(
            otherUser.getId().toString(),
            otherUser.getEmail(),
            otherUser.getRole().getId().toString(),
            otherUser.getRole().getValue(),
            otherUser.getFullName());
    /* Initialize Counter */
    Counter counter = new Counter();
    counter.setCreatedBy("system");
    counter.setUpdatedBy("system");
    counter.setNext(0);
    counterRepository.save(counter);
  }

  @AfterEach
  void tearDown() {
    deleteAll();
    counterRepository.deleteAll();
  }

  @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
  @Test
  void create_ShouldReturn200AndId_WhenRequestIsValid() throws Exception {
    CreateShipmentRequest request =
        new CreateShipmentRequest(stock.getId(), destWarehouse.getId(), new BigInteger("10"));
    mockMvc
        .perform(
            post("/api/shipment/create")
                .cookie(new Cookie(COOKIE_NAME, warehouseManagerJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").isNotEmpty());
    /* Verify stock allocated increased */
    Stock updatedStock = stockRepository.findById(stock.getId()).orElseThrow();
    assertEquals(10, updatedStock.getAllocated());
    /* Verify shipment created */
    assertTrue(shipmentRepository.count() > 0);
    Shipment shipment = shipmentRepository.findAll().get(0);
    assertEquals(10, shipment.getQuantity());
    /* Verify code format: ORG-DST-YYYY-M-D-1 (since next=0, +1 -> 1) */
    /* Actually exact date depends on execution time, but structure is checkable */
    assertTrue(shipment.getCode().startsWith("ORG-DST-"));
  }

  @Test
  void create_ShouldReturn403_WhenUserUnauthorized() throws Exception {
    CreateShipmentRequest request =
        new CreateShipmentRequest(stock.getId(), destWarehouse.getId(), new BigInteger("10"));
    mockMvc
        .perform(
            post("/api/shipment/create")
                .cookie(new Cookie(COOKIE_NAME, otherUserJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error", containsString("No Permission")));
  }

  @Test
  void create_ShouldReturn400_WhenUserNotAssignedToOriginWarehouse() throws Exception {
    /* Create another warehouse and stock */
    Warehouse otherWarehouse = createWarehouse("Other Warehouse", "OTH");
    Stock otherStock = createStock(otherWarehouse, product, 100, 0);
    CreateShipmentRequest request =
        new CreateShipmentRequest(otherStock.getId(), destWarehouse.getId(), new BigInteger("10"));
    mockMvc
        .perform(
            post("/api/shipment/create")
                .cookie(new Cookie(COOKIE_NAME, warehouseManagerJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", containsString(OPERATION_NOT_PERMITTED)));
  }

  @Test
  void create_ShouldReturn400_WhenStockNotFound() throws Exception {
    CreateShipmentRequest request =
        new CreateShipmentRequest(UUID.randomUUID(), destWarehouse.getId(), new BigInteger("10"));
    mockMvc
        .perform(
            post("/api/shipment/create")
                .cookie(new Cookie(COOKIE_NAME, warehouseManagerJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", containsString(STOCK_NOT_FOUND)));
  }

  @Test
  void create_ShouldReturn400_WhenDestWarehouseNotFound() throws Exception {
    CreateShipmentRequest request =
        new CreateShipmentRequest(stock.getId(), UUID.randomUUID(), new BigInteger("10"));
    mockMvc
        .perform(
            post("/api/shipment/create")
                .cookie(new Cookie(COOKIE_NAME, warehouseManagerJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", containsString(DEST_WAREHOUSE_NOT_FOUND)));
  }

  @Test
  void create_ShouldReturn400_WhenInsufficientStock() throws Exception {
    /* Stock is 100, allocated 0. Try 101. */
    CreateShipmentRequest request =
        new CreateShipmentRequest(stock.getId(), destWarehouse.getId(), new BigInteger("101"));
    mockMvc
        .perform(
            post("/api/shipment/create")
                .cookie(new Cookie(COOKIE_NAME, warehouseManagerJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", containsString(INSUFFICIENT_STOCK)));
  }

  @Test
  void create_ShouldReturn400_WhenQuantityIsZero() throws Exception {
    CreateShipmentRequest request =
        new CreateShipmentRequest(stock.getId(), destWarehouse.getId(), BigInteger.ZERO);
    mockMvc
        .perform(
            post("/api/shipment/create")
                .cookie(new Cookie(COOKIE_NAME, warehouseManagerJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", containsString("Quantity must be greater than 0")));
  }

  @Test
  void create_ShouldReturn400_WhenQuantityIsNegative() throws Exception {
    CreateShipmentRequest request =
        new CreateShipmentRequest(stock.getId(), destWarehouse.getId(), new BigInteger("-5"));
    mockMvc
        .perform(
            post("/api/shipment/create")
                .cookie(new Cookie(COOKIE_NAME, warehouseManagerJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", containsString("Quantity must be greater than 0")));
  }

  @Test
  void create_ShouldReturn400_WhenQuantityIsTooLarge() throws Exception {
    BigInteger largeQty = new BigInteger("1234567890"); /* 10 digits */
    CreateShipmentRequest request =
        new CreateShipmentRequest(stock.getId(), destWarehouse.getId(), largeQty);
    mockMvc
        .perform(
            post("/api/shipment/create")
                .cookie(new Cookie(COOKIE_NAME, warehouseManagerJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath(
                "$.error", containsString("Quantity is too large or has too many decimal places")));
  }
}
