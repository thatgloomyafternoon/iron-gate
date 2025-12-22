package com.fw.irongate.repositories.specs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fw.irongate.models.entities.Stock;
import com.fw.irongate.usecases.filter_stock.FilterStockRequest;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class TestStockSpecification {

  @Mock private Root<Stock> root;
  @Mock private CriteriaQuery<?> query;
  @Mock private CriteriaBuilder cb;
  @Mock private Path<Object> warehousePath;
  @Mock private Path<Object> productPath;
  @Mock private Path<String> stringPath;
  @Mock private Path<Integer> quantityPath;
  @Mock private Path<UUID> idPath;
  @Mock private Predicate predicate;
  @Mock private Expression<String> lowerExpression;

  @BeforeEach
  void setUp() {
    lenient().when(root.get("warehouse")).thenReturn(warehousePath);
    lenient().when(root.get("product")).thenReturn(productPath);
    lenient().when(root.<Integer>get("quantity")).thenReturn(quantityPath);
    lenient().when(warehousePath.<String>get("city")).thenReturn(stringPath);
    lenient().when(warehousePath.<UUID>get("id")).thenReturn(idPath);
    lenient().when(productPath.<String>get("name")).thenReturn(stringPath);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  void shouldFetchAssociations_WhenNotCountQuery() {
    /* Arrange */
    FilterStockRequest request = new FilterStockRequest(null, null, 0, 10);
    when(query.getResultType()).thenReturn((Class) Stock.class); /* Not Long.class */
    Specification<Stock> spec = StockSpecification.getSpecification(request, null);
    /* Act */
    spec.toPredicate(root, query, cb);
    /* Assert */
    verify(root).fetch("product");
    verify(root).fetch("warehouse");
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  void shouldNotFetchAssociations_WhenCountQuery() {
    /* Arrange */
    FilterStockRequest request = new FilterStockRequest(null, null, 0, 10);
    when(query.getResultType()).thenReturn((Class) Long.class);
    Specification<Stock> spec = StockSpecification.getSpecification(request, null);
    /* Act */
    spec.toPredicate(root, query, cb);
    /* Assert */
    verify(root, never()).fetch("product");
    verify(root, never()).fetch("warehouse");
  }

  @SuppressWarnings("unchecked")
  @Test
  void shouldReturnEmptyPredicate_WhenRequestIsEmpty() {
    /* Arrange */
    FilterStockRequest request = new FilterStockRequest(null, null, 0, 10);
    Specification<Stock> spec = StockSpecification.getSpecification(request, null);
    /* Act */
    spec.toPredicate(root, query, cb);
    /* Assert */
    verify(cb).and();
    verify(cb, never()).like(any(), anyString());
    verify(cb, never()).lessThanOrEqualTo(any(Expression.class), any(Integer.class));
  }

  @Test
  void shouldFilterByQuery_WhenQueryIsPresent() {
    /* Arrange */
    String search = "test";
    FilterStockRequest request = new FilterStockRequest(search, null, 0, 10);
    when(cb.lower(stringPath)).thenReturn(lowerExpression);
    when(cb.like(eq(lowerExpression), eq("%test%"))).thenReturn(predicate);
    when(cb.or(any(), any())).thenReturn(predicate);
    Specification<Stock> spec = StockSpecification.getSpecification(request, null);
    /* Act */
    spec.toPredicate(root, query, cb);
    /* Assert */
    verify(root).get("warehouse");
    verify(warehousePath).get("city");
    verify(root).get("product");
    verify(productPath).get("name");
    verify(cb, times(2)).like(eq(lowerExpression), eq("%test%"));
    verify(cb).or(any(Predicate.class), any(Predicate.class));
  }

  @Test
  void shouldFilterByMaxQuantity_WhenMaxQuantityIsPresent() {
    /* Arrange */
    Integer maxQty = 100;
    FilterStockRequest request = new FilterStockRequest(null, maxQty, 0, 10);
    Specification<Stock> spec = StockSpecification.getSpecification(request, null);
    /* Act */
    spec.toPredicate(root, query, cb);
    /* Assert */
    verify(root).get("quantity");
    verify(cb).lessThanOrEqualTo(quantityPath, maxQty);
  }

  @SuppressWarnings("unchecked")
  @Test
  void shouldFilterByWarehouseIds_WhenWarehouseIdsArePresent() {
    /* Arrange */
    List<UUID> warehouseIds = List.of(UUID.randomUUID(), UUID.randomUUID());
    FilterStockRequest request = new FilterStockRequest(null, null, 0, 10);
    /* Mock IN clause */
    CriteriaBuilder.In<UUID> inClause = mock(CriteriaBuilder.In.class);
    when(idPath.in(warehouseIds)).thenReturn(inClause);
    Specification<Stock> spec = StockSpecification.getSpecification(request, warehouseIds);
    /* Act */
    spec.toPredicate(root, query, cb);
    /* Assert */
    verify(root).get("warehouse");
    verify(warehousePath).get("id");
    verify(idPath).in(warehouseIds);
  }

  @SuppressWarnings("unchecked")
  @Test
  void shouldCombineAllFilters() {
    /* Arrange */
    String search = "test";
    Integer maxQty = 50;
    List<UUID> warehouseIds = List.of(UUID.randomUUID());
    FilterStockRequest request = new FilterStockRequest(search, maxQty, 0, 10);
    when(cb.lower(stringPath)).thenReturn(lowerExpression);
    when(cb.like(eq(lowerExpression), eq("%test%"))).thenReturn(predicate);
    when(cb.or(any(), any())).thenReturn(predicate);
    when(cb.lessThanOrEqualTo(eq(quantityPath), eq(maxQty))).thenReturn(predicate);
    CriteriaBuilder.In<UUID> inClause = mock(CriteriaBuilder.In.class);
    when(idPath.in(warehouseIds)).thenReturn(inClause);
    Specification<Stock> spec = StockSpecification.getSpecification(request, warehouseIds);
    /* Act */
    spec.toPredicate(root, query, cb);
    /* Assert */
    verify(cb).or(any(), any());
    verify(cb).lessThanOrEqualTo(quantityPath, maxQty);
    verify(idPath).in(warehouseIds);
    verify(cb).and(any(Predicate[].class));
  }
}
