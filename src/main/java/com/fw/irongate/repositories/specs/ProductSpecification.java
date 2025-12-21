package com.fw.irongate.repositories.specs;

import com.fw.irongate.models.entities.Product;
import com.fw.irongate.usecases.filter_product.FilterProductRequest;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {

  public static Specification<Product> getSpecification(FilterProductRequest request) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (request.query() != null && !request.query().isBlank()) {
        String searchPattern = "%" + request.query().toLowerCase() + "%";
        Predicate nameMatch =
            criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), searchPattern);
        Predicate skuMatch =
            criteriaBuilder.like(criteriaBuilder.lower(root.get("sku")), searchPattern);
        Predicate descriptionMatch =
            criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern);
        predicates.add(criteriaBuilder.or(nameMatch, skuMatch, descriptionMatch));
      }
      if (request.minPrice() != null) {
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), request.minPrice()));
      }
      if (request.maxPrice() != null) {
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), request.maxPrice()));
      }
      if (request.minQuantity() != null) {
        predicates.add(
            criteriaBuilder.greaterThanOrEqualTo(root.get("quantity"), request.minQuantity()));
      }
      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
