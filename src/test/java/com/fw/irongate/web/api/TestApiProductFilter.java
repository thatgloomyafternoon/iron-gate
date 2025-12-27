package com.fw.irongate.web.api;

import static com.fw.irongate.constants.SystemConstants.COOKIE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fw.irongate.models.dto.ProductDTO;
import com.fw.irongate.models.entities.Sysconfig;
import com.fw.irongate.models.entities.SysconfigType;
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
public class TestApiProductFilter extends TestParent {

  private Cookie cookie;

  @BeforeAll
  void beforeAll() throws Exception {
    setupCookie();
    createSomeProducts();
  }

  @AfterAll
  void afterAll() {
    deleteAll();
  }

  @Test
  void givenNoFilterParam_assert200_andCorrectData() throws Exception {
    /* test */
    String response =
        mockMvc
            .perform(
                get("/api/product/filter").cookie(cookie).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<ProductDTO> products =
        objectMapper.readValue(response, new TypeReference<>() {});
    ProductDTO productDTO = products.data().getFirst();
    assertEquals(0, products.currentPage());
    assertEquals(11, products.totalItems());
    assertEquals("Product 11", productDTO.name());
    assertEquals("TECH-004", productDTO.sku());
    assertEquals("asd", productDTO.description());
    assertEquals(new BigDecimal("20.20"), productDTO.price());
  }

  @Test
  void givenNameQuery_assert200_andCorrectReturnData() throws Exception {
    /* test */
    String response =
        mockMvc
            .perform(
                get("/api/product/filter")
                    .param("query", "product 1")
                    .cookie(cookie)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<ProductDTO> products =
        objectMapper.readValue(response, new TypeReference<>() {});
    assertEquals(0, products.currentPage());
    assertEquals(3, products.totalItems());
    assertEquals("Product 11", products.data().getFirst().name());
    assertEquals("TECH-004", products.data().getFirst().sku());
    assertEquals("asd", products.data().getFirst().description());
    assertEquals(new BigDecimal("20.20"), products.data().getFirst().price());
    assertEquals("Product 10", products.data().get(1).name());
    assertEquals("ACC-004", products.data().get(1).sku());
    assertEquals("asd", products.data().get(1).description());
    assertEquals(new BigDecimal("19.19"), products.data().get(1).price());
    assertEquals("Product 1", products.data().get(2).name());
    assertEquals("ACC-001", products.data().get(2).sku());
    assertEquals("desc", products.data().get(2).description());
    assertEquals(new BigDecimal("10.10"), products.data().get(2).price());
  }

  @Test
  void givenSku_assert200_andCorrectReturnData() throws Exception {
    /* test */
    String response =
        mockMvc
            .perform(
                get("/api/product/filter")
                    .param("query", "acc")
                    .cookie(cookie)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<ProductDTO> products =
        objectMapper.readValue(response, new TypeReference<>() {});
    assertEquals(0, products.currentPage());
    assertEquals(4, products.totalItems());
    assertEquals("Product 10", products.data().getFirst().name());
    assertEquals("ACC-004", products.data().getFirst().sku());
    assertEquals("asd", products.data().getFirst().description());
    assertEquals(new BigDecimal("19.19"), products.data().getFirst().price());
    assertEquals("Product 7", products.data().get(1).name());
    assertEquals("ACC-003", products.data().get(1).sku());
    assertEquals("asd", products.data().get(1).description());
    assertEquals(new BigDecimal("16.16"), products.data().get(1).price());
    assertEquals("Product 4", products.data().get(2).name());
    assertEquals("ACC-002", products.data().get(2).sku());
    assertEquals("desc", products.data().get(2).description());
    assertEquals(new BigDecimal("13.13"), products.data().get(2).price());
    assertEquals("Product 1", products.data().get(3).name());
    assertEquals("ACC-001", products.data().get(3).sku());
    assertEquals("desc", products.data().get(3).description());
    assertEquals(new BigDecimal("10.10"), products.data().get(3).price());
  }

  @Test
  void givenDesc_assert200_andCorrectReturnData() throws Exception {
    /* test */
    String response =
        mockMvc
            .perform(
                get("/api/product/filter")
                    .param("query", "desc")
                    .cookie(cookie)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<ProductDTO> products =
        objectMapper.readValue(response, new TypeReference<>() {});
    assertEquals(0, products.currentPage());
    assertEquals(5, products.totalItems());
    assertEquals("Product 5", products.data().getFirst().name());
    assertEquals("TECH-002", products.data().getFirst().sku());
    assertEquals("desc", products.data().getFirst().description());
    assertEquals(new BigDecimal("14.14"), products.data().getFirst().price());
    assertEquals("Product 4", products.data().get(1).name());
    assertEquals("ACC-002", products.data().get(1).sku());
    assertEquals("desc", products.data().get(1).description());
    assertEquals(new BigDecimal("13.13"), products.data().get(1).price());
    assertEquals("Product 3", products.data().get(2).name());
    assertEquals("FURN-001", products.data().get(2).sku());
    assertEquals("desc", products.data().get(2).description());
    assertEquals(new BigDecimal("12.12"), products.data().get(2).price());
    assertEquals("Product 2", products.data().get(3).name());
    assertEquals("TECH-001", products.data().get(3).sku());
    assertEquals("desc", products.data().get(3).description());
    assertEquals(new BigDecimal("11.11"), products.data().get(3).price());
    assertEquals("Product 1", products.data().get(4).name());
    assertEquals("ACC-001", products.data().get(4).sku());
    assertEquals("desc", products.data().get(4).description());
    assertEquals(new BigDecimal("10.10"), products.data().get(4).price());
  }

  @Test
  void givenMinPrice_assert200_andCorrectReturnData() throws Exception {
    /* test */
    String response =
        mockMvc
            .perform(
                get("/api/product/filter")
                    .param("minPrice", "19")
                    .cookie(cookie)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<ProductDTO> products =
        objectMapper.readValue(response, new TypeReference<>() {});
    assertEquals(0, products.currentPage());
    assertEquals(2, products.totalItems());
    assertEquals("Product 11", products.data().getFirst().name());
    assertEquals("TECH-004", products.data().getFirst().sku());
    assertEquals("asd", products.data().getFirst().description());
    assertEquals(new BigDecimal("20.20"), products.data().getFirst().price());
    assertEquals("Product 10", products.data().get(1).name());
    assertEquals("ACC-004", products.data().get(1).sku());
    assertEquals("asd", products.data().get(1).description());
    assertEquals(new BigDecimal("19.19"), products.data().get(1).price());
  }

  @Test
  void givenMaxPrice_assert200_andCorrectReturnData() throws Exception {
    /* test */
    String response =
        mockMvc
            .perform(
                get("/api/product/filter")
                    .param("maxPrice", "12")
                    .cookie(cookie)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<ProductDTO> products =
        objectMapper.readValue(response, new TypeReference<>() {});
    assertEquals(0, products.currentPage());
    assertEquals(2, products.totalItems());
    assertEquals("Product 2", products.data().getFirst().name());
    assertEquals("TECH-001", products.data().getFirst().sku());
    assertEquals("desc", products.data().getFirst().description());
    assertEquals(new BigDecimal("11.11"), products.data().getFirst().price());
    assertEquals("Product 1", products.data().get(1).name());
    assertEquals("ACC-001", products.data().get(1).sku());
    assertEquals("desc", products.data().get(1).description());
    assertEquals(new BigDecimal("10.10"), products.data().get(1).price());
  }

  @Test
  void givenNameAndMinPrice_assert200_andCorrectReturnData() throws Exception {
    /* test */
    String response =
        mockMvc
            .perform(
                get("/api/product/filter")
                    .param("query", "product 1")
                    .param("minPrice", "19")
                    .cookie(cookie)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<ProductDTO> products =
        objectMapper.readValue(response, new TypeReference<>() {});
    assertEquals(0, products.currentPage());
    assertEquals(2, products.totalItems());
    assertEquals("Product 11", products.data().getFirst().name());
    assertEquals("TECH-004", products.data().getFirst().sku());
    assertEquals("asd", products.data().getFirst().description());
    assertEquals(new BigDecimal("20.20"), products.data().getFirst().price());
    assertEquals("Product 10", products.data().get(1).name());
    assertEquals("ACC-004", products.data().get(1).sku());
    assertEquals("asd", products.data().get(1).description());
    assertEquals(new BigDecimal("19.19"), products.data().get(1).price());
  }

  @Test
  void givenNameAndMaxPrice_assert200_andCorrectReturnData() throws Exception {
    /* test */
    String response =
        mockMvc
            .perform(
                get("/api/product/filter")
                    .param("query", "product 1")
                    .param("maxPrice", "20")
                    .cookie(cookie)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<ProductDTO> products =
        objectMapper.readValue(response, new TypeReference<>() {});
    assertEquals(0, products.currentPage());
    assertEquals(2, products.totalItems());
    assertEquals("Product 10", products.data().getFirst().name());
    assertEquals("ACC-004", products.data().getFirst().sku());
    assertEquals("asd", products.data().getFirst().description());
    assertEquals(new BigDecimal("19.19"), products.data().getFirst().price());
    assertEquals("Product 1", products.data().get(1).name());
    assertEquals("ACC-001", products.data().get(1).sku());
    assertEquals("desc", products.data().get(1).description());
    assertEquals(new BigDecimal("10.10"), products.data().get(1).price());
  }

  @Test
  void givenSkuAndMinPrice_assert200_andCorrectReturnData() throws Exception {
    /* test */
    String response =
        mockMvc
            .perform(
                get("/api/product/filter")
                    .param("query", "tech")
                    .param("minPrice", "20")
                    .cookie(cookie)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<ProductDTO> products =
        objectMapper.readValue(response, new TypeReference<>() {});
    assertEquals(0, products.currentPage());
    assertEquals(1, products.totalItems());
    assertEquals("Product 11", products.data().getFirst().name());
    assertEquals("TECH-004", products.data().getFirst().sku());
    assertEquals("asd", products.data().getFirst().description());
    assertEquals(new BigDecimal("20.20"), products.data().getFirst().price());
  }

  @Test
  void givenSkuAndMaxPrice_assert200_andCorrectReturnData() throws Exception {
    /* test */
    String response =
        mockMvc
            .perform(
                get("/api/product/filter")
                    .param("query", "furn")
                    .param("maxPrice", "16")
                    .cookie(cookie)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<ProductDTO> products =
        objectMapper.readValue(response, new TypeReference<>() {});
    assertEquals(0, products.currentPage());
    assertEquals(2, products.totalItems());
    assertEquals("Product 6", products.data().getFirst().name());
    assertEquals("FURN-002", products.data().getFirst().sku());
    assertEquals("asd", products.data().getFirst().description());
    assertEquals(new BigDecimal("15.15"), products.data().getFirst().price());
    assertEquals("Product 3", products.data().get(1).name());
    assertEquals("FURN-001", products.data().get(1).sku());
    assertEquals("desc", products.data().get(1).description());
    assertEquals(new BigDecimal("12.12"), products.data().get(1).price());
  }

  @Test
  void givenDescAndMinPrice_assert200_andCorrectReturnData() throws Exception {
    /* test */
    String response =
        mockMvc
            .perform(
                get("/api/product/filter")
                    .param("query", "desc")
                    .param("minPrice", "13")
                    .cookie(cookie)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<ProductDTO> products =
        objectMapper.readValue(response, new TypeReference<>() {});
    assertEquals(0, products.currentPage());
    assertEquals(2, products.totalItems());
    assertEquals("Product 5", products.data().getFirst().name());
    assertEquals("TECH-002", products.data().getFirst().sku());
    assertEquals("desc", products.data().getFirst().description());
    assertEquals(new BigDecimal("14.14"), products.data().getFirst().price());
    assertEquals("Product 4", products.data().get(1).name());
    assertEquals("ACC-002", products.data().get(1).sku());
    assertEquals("desc", products.data().get(1).description());
    assertEquals(new BigDecimal("13.13"), products.data().get(1).price());
  }

  @Test
  void givenDescAndMaxPrice_assert200_andCorrectReturnData() throws Exception {
    /* test */
    String response =
        mockMvc
            .perform(
                get("/api/product/filter")
                    .param("query", "asd")
                    .param("maxPrice", "17")
                    .cookie(cookie)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    PaginatedResponse<ProductDTO> products =
        objectMapper.readValue(response, new TypeReference<>() {});
    assertEquals(0, products.currentPage());
    assertEquals(2, products.totalItems());
    assertEquals("Product 7", products.data().getFirst().name());
    assertEquals("ACC-003", products.data().getFirst().sku());
    assertEquals("asd", products.data().getFirst().description());
    assertEquals(new BigDecimal("16.16"), products.data().getFirst().price());
    assertEquals("Product 6", products.data().get(1).name());
    assertEquals("FURN-002", products.data().get(1).sku());
    assertEquals("asd", products.data().get(1).description());
    assertEquals(new BigDecimal("15.15"), products.data().get(1).price());
  }

  private void setupCookie() throws Exception {
    if (cookie == null) {
      SysconfigType r = createSysconfigType("ROLE", "desc");
      SysconfigType rp = createSysconfigType("RESOURCE_PATH", "desc");
      Sysconfig rAm = createSysconfig(r, "AREA_MANAGER", "Area Manager");
      Sysconfig rpApf = createSysconfig(rp, "API_PRODUCT_FILTER", "/api/product/filter");
      createUser(rAm, "am@mail.com", bCryptPasswordEncoder.encode("password"), "full name");
      createPermission(rAm, rpApf);
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
    }
  }

  private void createSomeProducts() {
    if (productRepository.findAll().isEmpty()) {
      createProduct("Product 1", "ACC-001", "desc", new BigDecimal("10.10"));
      createProduct("Product 2", "TECH-001", "desc", new BigDecimal("11.11"));
      createProduct("Product 3", "FURN-001", "desc", new BigDecimal("12.12"));
      createProduct("Product 4", "ACC-002", "desc", new BigDecimal("13.13"));
      createProduct("Product 5", "TECH-002", "desc", new BigDecimal("14.14"));
      createProduct("Product 6", "FURN-002", "asd", new BigDecimal("15.15"));
      createProduct("Product 7", "ACC-003", "asd", new BigDecimal("16.16"));
      createProduct("Product 8", "TECH-003", "asd", new BigDecimal("17.17"));
      createProduct("Product 9", "FURN-003", "asd", new BigDecimal("18.18"));
      createProduct("Product 10", "ACC-004", "asd", new BigDecimal("19.19"));
      createProduct("Product 11", "TECH-004", "asd", new BigDecimal("20.20"));
    }
  }
}
