package com.fw.irongate.usecases.filter_warehouse;

public record FilterWarehouseRequest(String query, int page, int size) {

  public FilterWarehouseRequest {
    if (page < 0) page = 0;
    if (size <= 0) size = 10;
  }
}
