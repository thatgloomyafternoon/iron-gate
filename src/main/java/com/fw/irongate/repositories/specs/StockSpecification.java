package com.fw.irongate.repositories.specs;

import com.fw.irongate.models.entities.Stock;
import com.fw.irongate.usecases.filter_stock.FilterStockRequest;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class StockSpecification {

  public static Specification<Stock> getSpecification(
      FilterStockRequest request, List<UUID> warehouseIds) {
    return (root, query, criteriaBuilder) -> {
      assert query != null;
      if (Long.class != query.getResultType()) {
        root.fetch("product");
        root.fetch("warehouse");
      }
      List<Predicate> predicates = new ArrayList<>();
      if (request.query() != null && !request.query().isBlank()) {
        String searchPattern = "%" + request.query().toLowerCase() + "%";
        Predicate warehouseNameMatch =
            criteriaBuilder.like(
                criteriaBuilder.lower(root.get("warehouse").get("name")), searchPattern);
        Predicate productNameMatch =
            criteriaBuilder.like(
                criteriaBuilder.lower(root.get("product").get("name")), searchPattern);
        Predicate skuMatch =
            criteriaBuilder.like(
                criteriaBuilder.lower(root.get("product").get("sku")), searchPattern);
        predicates.add(criteriaBuilder.or(warehouseNameMatch, productNameMatch, skuMatch));
      }
      if (request.maxQuantity() != null) {
        predicates.add(
            criteriaBuilder.lessThanOrEqualTo(root.get("quantity"), request.maxQuantity()));
      }
      if (warehouseIds != null && !warehouseIds.isEmpty()) {
        predicates.add(root.get("warehouse").get("id").in(warehouseIds));
      }
      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
