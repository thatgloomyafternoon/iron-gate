package com.fw.irongate.web.api;

import static com.fw.irongate.constants.MessageConstants.SKU_ALREADY_EXISTS;
import static com.fw.irongate.constants.SystemConstants.COOKIE_NAME;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fw.irongate.models.entities.Product;
import com.fw.irongate.models.entities.Sysconfig;
import com.fw.irongate.models.entities.SysconfigType;
import com.fw.irongate.models.entities.User;
import com.fw.irongate.usecases.create_product.CreateProductRequest;
import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
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
class TestApiProductCreate extends TestParent {

  private SysconfigType roleType;
  private SysconfigType resourcePathType;
  private Sysconfig roleAreaManager;
  private Sysconfig roleWarehouseManager;
  private Sysconfig resourceCreateProduct;
  private User areaManager;
  private User warehouseManager;
  private String areaManagerJwt;
  private String warehouseManagerJwt;

  @BeforeEach
  void setUp() {
    roleType = createSysconfigType("ROLE", "Role Type");
    resourcePathType = createSysconfigType("RESOURCE_PATH", "Resource Path Type");
    roleAreaManager = createSysconfig(roleType, "AREA_MANAGER", "Area Manager");
    roleWarehouseManager = createSysconfig(roleType, "WAREHOUSE_MANAGER", "Warehouse Manager");
    resourceCreateProduct =
        createSysconfig(resourcePathType, "PRODUCT_CREATE", "/api/product/create");
    createPermission(roleAreaManager, resourceCreateProduct);
    areaManager = createUser(roleAreaManager, "am@example.com", "hash", "Area Manager");
    warehouseManager =
        createUser(roleWarehouseManager, "wm@example.com", "hash", "Warehouse Manager");
    areaManagerJwt =
        jwtUtil.generateJwt(
            areaManager.getId().toString(),
            areaManager.getEmail(),
            areaManager.getRole().getId().toString(),
            areaManager.getRole().getValue(),
            areaManager.getFullName());
    warehouseManagerJwt =
        jwtUtil.generateJwt(
            warehouseManager.getId().toString(),
            warehouseManager.getEmail(),
            warehouseManager.getRole().getId().toString(),
            warehouseManager.getRole().getValue(),
            warehouseManager.getFullName());
  }

  @AfterEach
  void tearDown() {
    deleteAll();
  }

  @Test
  void create_ShouldReturn200AndId_WhenRequestIsValidAndAuthorized() throws Exception {
    CreateProductRequest request =
        new CreateProductRequest(
            "Test Product", "SKU-123", "Description", new BigDecimal("100.50"));
    mockMvc
        .perform(
            post("/api/product/create")
                .cookie(new Cookie(COOKIE_NAME, areaManagerJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").isNotEmpty());
    assertTrue(productRepository.findBySku("SKU-123").isPresent());
    Product product = productRepository.findBySku("SKU-123").get();
    assertEquals("Test Product", product.getName());
    assertEquals(0, new BigDecimal("100.50").compareTo(product.getPrice()));
  }

  @Test
  void create_ShouldReturn403_WhenUserUnauthorized() throws Exception {
    CreateProductRequest request =
        new CreateProductRequest(
            "Test Product", "SKU-123", "Description", new BigDecimal("100.50"));
    mockMvc
        .perform(
            post("/api/product/create")
                .cookie(new Cookie(COOKIE_NAME, warehouseManagerJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error", containsString("No Permission")));
  }

  @Test
  void create_ShouldReturn400_WhenSkuAlreadyExists() throws Exception {
    /* 1. Create initial product */
    createProduct("Existing Product", "SKU-EXISTING", "Desc", new BigDecimal("50.00"));
    /* 2. Try to create another with same SKU */
    CreateProductRequest request =
        new CreateProductRequest("New Product", "SKU-EXISTING", "Desc", new BigDecimal("100.00"));
    mockMvc
        .perform(
            post("/api/product/create")
                .cookie(new Cookie(COOKIE_NAME, areaManagerJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", containsString(SKU_ALREADY_EXISTS)));
  }

  @Test
  void create_ShouldReturn400_WhenNameIsBlank() throws Exception {
    CreateProductRequest request =
        new CreateProductRequest("", "SKU-123", "Desc", new BigDecimal("100.00"));
    mockMvc
        .perform(
            post("/api/product/create")
                .cookie(new Cookie(COOKIE_NAME, areaManagerJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", containsString("Name cannot be blank")));
  }

  @Test
  void create_ShouldReturn400_WhenNameIsTooLong() throws Exception {
    String longName = "a".repeat(41);
    CreateProductRequest request =
        new CreateProductRequest(longName, "SKU-123", "Desc", new BigDecimal("100.00"));
    mockMvc
        .perform(
            post("/api/product/create")
                .cookie(new Cookie(COOKIE_NAME, areaManagerJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", containsString("Name max length 40")));
  }

  @Test
  void create_ShouldReturn400_WhenSkuIsBlank() throws Exception {
    CreateProductRequest request =
        new CreateProductRequest("Product", "", "Desc", new BigDecimal("100.00"));
    mockMvc
        .perform(
            post("/api/product/create")
                .cookie(new Cookie(COOKIE_NAME, areaManagerJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", containsString("SKU cannot be blank")));
  }

  @Test
  void create_ShouldReturn400_WhenSkuIsTooLong() throws Exception {
    String longSku = "a".repeat(16);
    CreateProductRequest request =
        new CreateProductRequest("Product", longSku, "Desc", new BigDecimal("100.00"));
    mockMvc
        .perform(
            post("/api/product/create")
                .cookie(new Cookie(COOKIE_NAME, areaManagerJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", containsString("SKU max length 15")));
  }

  @Test
  void create_ShouldReturn400_WhenDescriptionIsTooLong() throws Exception {
    String longDesc = "a".repeat(41);
    CreateProductRequest request =
        new CreateProductRequest("Product", "SKU-123", longDesc, new BigDecimal("100.00"));
    mockMvc
        .perform(
            post("/api/product/create")
                .cookie(new Cookie(COOKIE_NAME, areaManagerJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", containsString("Description max length 40")));
  }

  @Test
  void create_ShouldReturn400_WhenPriceIsNull() throws Exception {
    CreateProductRequest request = new CreateProductRequest("Product", "SKU-123", "Desc", null);
    mockMvc
        .perform(
            post("/api/product/create")
                .cookie(new Cookie(COOKIE_NAME, areaManagerJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
    /* Error message depends on which validation fails first or if default message is used */
  }

  @Test
  void create_ShouldReturn400_WhenPriceIsNegative() throws Exception {
    CreateProductRequest request =
        new CreateProductRequest("Product", "SKU-123", "Desc", new BigDecimal("-1.00"));
    mockMvc
        .perform(
            post("/api/product/create")
                .cookie(new Cookie(COOKIE_NAME, areaManagerJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", containsString("Price must be positive")));
  }

  @Test
  void create_ShouldReturn400_WhenPriceHasTooManyDecimals() throws Exception {
    CreateProductRequest request =
        new CreateProductRequest("Product", "SKU-123", "Desc", new BigDecimal("10.001"));
    mockMvc
        .perform(
            post("/api/product/create")
                .cookie(new Cookie(COOKIE_NAME, areaManagerJwt))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath(
                "$.error", containsString("Price is too large or has too many decimal places")));
  }
}
