package com.fw.irongate.repositories.specs;

import com.fw.irongate.models.entities.Shipment;
import com.fw.irongate.usecases.filter_shipment.FilterShipmentRequest;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class ShipmentSpecification {

  public static Specification<Shipment> getSpecification(
      FilterShipmentRequest request, List<UUID> warehouseIds) {
    return (root, query, criteriaBuilder) -> {
      assert query != null;
      if (Long.class != query.getResultType()) {
        root.fetch("stock");
        root.fetch("stock").fetch("warehouse");
        root.fetch("stock").fetch("product");
        root.fetch("destWarehouse");
      }
      List<Predicate> predicates = new ArrayList<>();
      if (request.code() != null && !request.code().isBlank()) {
        String searchPattern = "%" + request.code().toLowerCase() + "%";
        Predicate codeMatch =
            criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), searchPattern);
        predicates.add(codeMatch);
      }
      if (request.productName() != null && !request.productName().isBlank()) {
        String searchPattern = "%" + request.productName().toLowerCase() + "%";
        Predicate productNameMatch =
            criteriaBuilder.like(
                criteriaBuilder.lower(root.get("stock").get("product").get("name")), searchPattern);
        predicates.add(productNameMatch);
      }
      if (request.minQuantity() != null) {
        predicates.add(
            criteriaBuilder.greaterThanOrEqualTo(root.get("quantity"), request.minQuantity()));
      }
      if (request.maxQuantity() != null) {
        predicates.add(
            criteriaBuilder.lessThanOrEqualTo(root.get("quantity"), request.maxQuantity()));
      }
      if (request.from() != null && !request.from().isBlank()) {
        String searchPattern = "%" + request.from().toLowerCase() + "%";
        Predicate fromMatch =
            criteriaBuilder.like(
                criteriaBuilder.lower(root.get("stock").get("warehouse").get("name")),
                searchPattern);
        predicates.add(fromMatch);
      }
      if (request.to() != null && !request.to().isBlank()) {
        String searchPattern = "%" + request.to().toLowerCase() + "%";
        Predicate toMatch =
            criteriaBuilder.like(
                criteriaBuilder.lower(root.get("destWarehouse").get("name")), searchPattern);
        predicates.add(toMatch);
      }
      if (request.status() != null && !request.status().isBlank()) {
        String searchPattern = "%" + request.status().toLowerCase() + "%";
        Predicate statusMatch =
            criteriaBuilder.like(criteriaBuilder.lower(root.get("status")), searchPattern);
        predicates.add(statusMatch);
      }
      if (request.assignedTo() != null && !request.assignedTo().isBlank()) {
        String searchPattern = "%" + request.assignedTo().toLowerCase() + "%";
        Predicate assignedToMatch =
            criteriaBuilder.like(criteriaBuilder.lower(root.get("assignedTo")), searchPattern);
        predicates.add(assignedToMatch);
      }
      if (warehouseIds != null && !warehouseIds.isEmpty()) {
        predicates.add(root.get("stock").get("warehouse").get("id").in(warehouseIds));
      }
      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
