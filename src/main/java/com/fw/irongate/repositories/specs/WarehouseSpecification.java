package com.fw.irongate.repositories.specs;

import com.fw.irongate.models.entities.Warehouse;
import com.fw.irongate.usecases.filter_warehouse.FilterWarehouseRequest;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class WarehouseSpecification {

  public static Specification<Warehouse> getSpecification(FilterWarehouseRequest request) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (request.query() != null && !request.query().isBlank()) {
        String searchPattern = "%" + request.query().toLowerCase() + "%";
        Predicate nameMatch =
            criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchPattern);
        Predicate codeMatch =
            criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), searchPattern);
        predicates.add(criteriaBuilder.or(nameMatch, codeMatch));
      }
      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
