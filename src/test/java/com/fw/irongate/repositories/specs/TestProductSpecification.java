package com.fw.irongate.repositories.specs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fw.irongate.models.entities.Product;
import com.fw.irongate.usecases.filter_product.FilterProductRequest;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class ProductSpecificationTest {

  @Mock private Root<Product> root;
  @Mock private CriteriaQuery<?> query;
  @Mock private CriteriaBuilder cb;
  @Mock private Path<String> stringPath;
  @Mock private Path<BigDecimal> pricePath;
  @Mock private Predicate predicate;
  @Mock private Expression<String> lowerExpression;

  @BeforeEach
  void setUp() {
    /* Default lenient stubs to avoid unnecessary stubbing errors for paths */
    /* We use lenient because not every test uses every path */
    lenient().when(root.<String>get("name")).thenReturn(stringPath);
    lenient().when(root.<String>get("sku")).thenReturn(stringPath);
    lenient().when(root.<String>get("description")).thenReturn(stringPath);
    lenient().when(root.<BigDecimal>get("price")).thenReturn(pricePath);
  }

  @SuppressWarnings("unchecked")
  @Test
  void shouldReturnEmptyPredicate_WhenRequestIsEmpty() {
    /* Arrange */
    FilterProductRequest request = new FilterProductRequest(null, null, null, 0, 10);
    Specification<Product> spec = ProductSpecification.getSpecification(request);
    /* Act */
    spec.toPredicate(root, query, cb);
    /* Assert */
    /* Should only call cb.and() with an empty array or no predicates */
    verify(cb).and();
    verify(cb, never()).like(any(), anyString());
    verify(cb, never()).greaterThanOrEqualTo(any(Expression.class), any(BigDecimal.class));
  }

  @Test
  void shouldFilterByQuery_WhenQueryIsPresent() {
    /* Arrange */
    String search = "apple";
    FilterProductRequest request = new FilterProductRequest(search, null, null, 0, 10);
    /* Mock string operations */
    when(cb.lower(stringPath)).thenReturn(lowerExpression);
    when(cb.like(eq(lowerExpression), eq("%apple%"))).thenReturn(predicate);
    when(cb.or(any(), any(), any())).thenReturn(predicate);
    Specification<Product> spec = ProductSpecification.getSpecification(request);
    /* Act */
    spec.toPredicate(root, query, cb);
    /* Assert */
    /* Verify we accessed name, sku, and description */
    verify(root).get("name");
    verify(root).get("sku");
    verify(root).get("description");
    /* Verify logic */
    verify(cb, times(3)).like(eq(lowerExpression), eq("%apple%"));
    verify(cb).or(any(Predicate.class), any(Predicate.class), any(Predicate.class));
  }

  @Test
  void shouldNotFilterByQuery_WhenQueryIsBlank() {
    /* Arrange */
    FilterProductRequest request = new FilterProductRequest("   ", null, null, 0, 10);
    Specification<Product> spec = ProductSpecification.getSpecification(request);
    /* Act */
    spec.toPredicate(root, query, cb);
    /* Assert */
    verify(cb, never()).like(any(), anyString());
  }

  @Test
  void shouldFilterByMinPrice_WhenMinPriceIsPresent() {
    /* Arrange */
    BigDecimal minPrice = BigDecimal.TEN;
    FilterProductRequest request = new FilterProductRequest(null, minPrice, null, 0, 10);
    Specification<Product> spec = ProductSpecification.getSpecification(request);
    /* Act */
    spec.toPredicate(root, query, cb);
    /* Assert */
    verify(root).get("price");
    verify(cb).greaterThanOrEqualTo(pricePath, minPrice);
  }

  @Test
  void shouldFilterByMaxPrice_WhenMaxPriceIsPresent() {
    /* Arrange */
    BigDecimal maxPrice = new BigDecimal("100.00");
    FilterProductRequest request = new FilterProductRequest(null, null, maxPrice, 0, 10);
    Specification<Product> spec = ProductSpecification.getSpecification(request);
    /* Act */
    spec.toPredicate(root, query, cb);
    /* Assert */
    verify(root).get("price");
    verify(cb).lessThanOrEqualTo(pricePath, maxPrice);
  }

  @Test
  void shouldCombineAllFilters_WhenAllFieldsArePresent() {
    /* Arrange */
    FilterProductRequest request =
        new FilterProductRequest("test", BigDecimal.ONE, BigDecimal.TEN, 0, 10);
    /* Mock string ops again for the query part */
    when(cb.lower(stringPath)).thenReturn(lowerExpression);
    when(cb.like(any(), anyString())).thenReturn(predicate);
    when(cb.or(any(), any(), any())).thenReturn(predicate);
    /* Mock return for price/qty predicates so, they can be bundled into the final AND */
    when(cb.greaterThanOrEqualTo(eq(pricePath), any(BigDecimal.class))).thenReturn(predicate);
    when(cb.lessThanOrEqualTo(eq(pricePath), any(BigDecimal.class))).thenReturn(predicate);
    Specification<Product> spec = ProductSpecification.getSpecification(request);
    /* Act */
    spec.toPredicate(root, query, cb);
    /* Assert */
    /* Verify all criteria builder methods were called */
    verify(cb).or(any(), any(), any());
    verify(cb).greaterThanOrEqualTo(pricePath, BigDecimal.ONE);
    verify(cb).lessThanOrEqualTo(pricePath, BigDecimal.TEN);
    /* Verify final combination. */
    /* We expect 4 predicates (1 OR group + 3 explicit filters) passed to the final AND */
    /* Note: The implementation converts List<Predicate> to array. */
    verify(cb).and(any(Predicate[].class));
  }
}
