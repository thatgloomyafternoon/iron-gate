package com.fw.irongate.usecases.filter_stock;

public record FilterStockRequest(
    String warehouseName, String productName, Integer maxQuantity, int page, int size) {

  public FilterStockRequest {
    if (page < 0) page = 0;
    if (size <= 0) size = 10;
  }
}
