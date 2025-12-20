package com.fw.irongate.web.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fw.irongate.usecases.create_product.CreateProductRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TestApiProductCreateValidation {

  private static Validator validator;

  @BeforeAll
  static void setUp() {
    /* Manually build the Validator factory (No Spring Context needed) */
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void shouldPass_WhenAllFieldsAreValid() {
    CreateProductRequest request =
        new CreateProductRequest("Valid Name", "SKU-123", "Desc", new BigDecimal("99.99"), 10);
    Set<ConstraintViolation<CreateProductRequest>> violations = validator.validate(request);
    assertTrue(violations.isEmpty(), "Expected no violations for valid request");
  }

  @Test
  void shouldFail_WhenNameIsBlank() {
    CreateProductRequest request =
        new CreateProductRequest("", /* Blank */ "SKU-123", "Desc", BigDecimal.TEN, 10);
    Set<ConstraintViolation<CreateProductRequest>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    assertEquals("Name cannot be blank", violations.iterator().next().getMessage());
  }

  @Test
  void shouldFail_WhenPriceIsNegative() {
    CreateProductRequest request =
        new CreateProductRequest("Name", "SKU", "Desc", new BigDecimal("-1.00"), /* Negative */ 10);
    Set<ConstraintViolation<CreateProductRequest>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    assertEquals("Price must be positive", violations.iterator().next().getMessage());
  }

  @Test
  void shouldFail_WhenPriceIsTooLarge_Or_TooManyDecimals() {
    /* Case 1: Too many integer digits (Max 17) */
    CreateProductRequest largePrice =
        new CreateProductRequest(
            "Name", "SKU", "Desc", new BigDecimal("100000000000000000.00"), /* 18 digits */ 10);
    Set<ConstraintViolation<CreateProductRequest>> v1 = validator.validate(largePrice);
    assertFalse(v1.isEmpty(), "Should fail for price with > 17 integer digits");
    /* Case 2: Too many decimal places (Max 2) */
    CreateProductRequest precisePrice =
        new CreateProductRequest(
            "Name", "SKU", "Desc", new BigDecimal("10.999"), /* 3 decimals */ 10);
    Set<ConstraintViolation<CreateProductRequest>> v2 = validator.validate(precisePrice);
    assertFalse(v2.isEmpty(), "Should fail for price with > 2 decimal places");
  }

  @SuppressWarnings("DataFlowIssue")
  @Test
  void shouldFail_WhenQuantityIsNegative() {
    CreateProductRequest request =
        new CreateProductRequest("Name", "SKU", "Desc", BigDecimal.TEN, -1 /* Negative */);
    Set<ConstraintViolation<CreateProductRequest>> violations = validator.validate(request);
    assertFalse(violations.isEmpty());
    assertEquals("Quantity must be positive or zero", violations.iterator().next().getMessage());
  }

  @Test
  void shouldPass_WhenQuantityIsZero() {
    CreateProductRequest request =
        new CreateProductRequest(
            "Name", "SKU", "Desc", BigDecimal.TEN, 0 /* Zero is valid (@PositiveOrZero) */);
    Set<ConstraintViolation<CreateProductRequest>> violations = validator.validate(request);
    assertTrue(violations.isEmpty());
  }
}
