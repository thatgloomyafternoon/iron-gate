package com.fw.irongate.usecases.filter_order;

import java.math.BigDecimal;

public record FilterOrderRequest(
    String customerName,
    String toWarehouse,
    String productName,
    BigDecimal minTotalPrice,
    BigDecimal maxTotalPrice,
    String status,
    int page,
    int size) {

  public FilterOrderRequest {
    if (page < 0) page = 0;
    if (size <= 0) size = 10;
  }
}
