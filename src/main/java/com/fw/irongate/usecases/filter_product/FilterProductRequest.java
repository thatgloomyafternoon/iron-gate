package com.fw.irongate.usecases.filter_product;

import java.math.BigDecimal;

public record FilterProductRequest(
    String query,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    Integer minQuantity,
    int page,
    int size) {

  public FilterProductRequest {
    if (page < 0) page = 0;
    if (size <= 0) size = 10;
  }
}
