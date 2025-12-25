package com.fw.irongate.usecases.filter_order;

public record FilterOrderRequest(String query, int page, int size) {

  public FilterOrderRequest {
    if (page < 0) page = 0;
    if (size <= 0) size = 10;
  }
}
