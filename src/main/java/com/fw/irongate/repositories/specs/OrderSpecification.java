package com.fw.irongate.repositories.specs;

import com.fw.irongate.models.entities.Order;
import com.fw.irongate.models.entities.OrderProduct;
import com.fw.irongate.usecases.filter_order.FilterOrderRequest;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class OrderSpecification {

  public static Specification<Order> getSpecification(
      FilterOrderRequest request, List<UUID> warehouseIds) {
    return (root, query, criteriaBuilder) -> {
      assert query != null;
      if (Long.class != query.getResultType()) {
        root.fetch("warehouse", JoinType.LEFT);
        Fetch<Order, OrderProduct> orderProductsFetch = root.fetch("orderProducts", JoinType.LEFT);
        orderProductsFetch.fetch("product", JoinType.LEFT);
        /* Important: distinct is needed when fetching collections to avoid duplicate root entities */
        /* in the result list */
        query.distinct(true);
      }
      List<Predicate> predicates = new ArrayList<>();
      if (request.customerName() != null && !request.customerName().isBlank()) {
        String searchPattern = "%" + request.customerName().toLowerCase() + "%";
        Predicate customerNameMatch =
            criteriaBuilder.like(criteriaBuilder.lower(root.get("customerName")), searchPattern);
        predicates.add(customerNameMatch);
      }
      if (request.toWarehouse() != null && !request.toWarehouse().isBlank()) {
        String searchPattern = "%" + request.toWarehouse().toLowerCase() + "%";
        Predicate warehouseNameMatch =
            criteriaBuilder.like(
                criteriaBuilder.lower(root.get("warehouse").get("name")), searchPattern);
        predicates.add(warehouseNameMatch);
      }
      if (request.productName() != null && !request.productName().isBlank()) {
        String searchPattern = "%" + request.productName().toLowerCase() + "%";
        Predicate productNameMatch =
            criteriaBuilder.like(
                criteriaBuilder.lower(root.get("orderProducts").get("product").get("name")),
                searchPattern);
        predicates.add(productNameMatch);
      }
      if (request.minTotalPrice() != null) {
        predicates.add(
            criteriaBuilder.greaterThanOrEqualTo(root.get("totalPrice"), request.minTotalPrice()));
      }
      if (request.maxTotalPrice() != null) {
        predicates.add(
            criteriaBuilder.lessThanOrEqualTo(root.get("totalPrice"), request.maxTotalPrice()));
      }
      if (request.status() != null && !request.status().isBlank()) {
        String searchPattern = "%" + request.status().toLowerCase() + "%";
        Predicate statusMatch =
            criteriaBuilder.like(criteriaBuilder.lower(root.get("status")), searchPattern);
        predicates.add(statusMatch);
      }
      if (warehouseIds != null && !warehouseIds.isEmpty()) {
        predicates.add(root.get("warehouse").get("id").in(warehouseIds));
      }
      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
