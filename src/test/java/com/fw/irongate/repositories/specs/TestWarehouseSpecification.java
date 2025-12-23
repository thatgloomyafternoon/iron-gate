package com.fw.irongate.repositories.specs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fw.irongate.models.entities.Warehouse;
import com.fw.irongate.usecases.filter_warehouse.FilterWarehouseRequest;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class TestWarehouseSpecification {

  @Mock private Root<Warehouse> root;
  @Mock private CriteriaQuery<?> query;
  @Mock private CriteriaBuilder cb;
  @Mock private Path<String> stringPath;
  @Mock private Predicate predicate;
  @Mock private Expression<String> lowerExpression;

  @BeforeEach
  void setUp() {
    lenient().when(root.<String>get("name")).thenReturn(stringPath);
    lenient().when(root.<String>get("code")).thenReturn(stringPath);
  }

  @Test
  void shouldReturnEmptyPredicate_WhenQueryIsBlank() {
    /* 1. Arrange */
    FilterWarehouseRequest request = new FilterWarehouseRequest("   ", 0, 10);
    Specification<Warehouse> spec = WarehouseSpecification.getSpecification(request);
    /* 2. Act */
    spec.toPredicate(root, query, cb);
    /* 3. Assert */
    verify(cb).and();
    verify(cb, never()).like(any(), anyString());
  }

  @Test
  void shouldReturnEmptyPredicate_WhenQueryIsNull() {
    /* 1. Arrange */
    FilterWarehouseRequest request = new FilterWarehouseRequest(null, 0, 10);
    Specification<Warehouse> spec = WarehouseSpecification.getSpecification(request);
    /* 2. Act */
    spec.toPredicate(root, query, cb);
    /* 3. Assert */
    verify(cb).and();
    verify(cb, never()).like(any(), anyString());
  }

  @Test
  void shouldFilterByNameOrCode_WhenQueryIsPresent() {
    /* 1. Arrange */
    String searchText = "NYC";
    FilterWarehouseRequest request = new FilterWarehouseRequest(searchText, 0, 10);
    String pattern = "%nyc%";
    /* Mock string lower() and like() calls */
    when(cb.lower(stringPath)).thenReturn(lowerExpression);
    when(cb.like(eq(lowerExpression), eq(pattern))).thenReturn(predicate);
    when(cb.or(any(), any())).thenReturn(predicate);
    Specification<Warehouse> spec = WarehouseSpecification.getSpecification(request);
    /* 2. Act */
    spec.toPredicate(root, query, cb);
    /* 3. Assert */
    /* Verify access to fields */
    verify(root).get("name");
    verify(root).get("code");
    /* Verify logic */
    verify(cb, times(2))
        .like(
            eq(lowerExpression),
            eq(pattern)); /* Called twice, mocked once per call signature effectively */
    verify(cb).or(any(Predicate.class), any(Predicate.class));
    /* Verify final AND */
    verify(cb).and(any(Predicate[].class));
  }
}
