package com.fw.irongate.models.dto;

import java.math.BigDecimal;

@SuppressWarnings("unused")
public record ChartDataDTO(String label, Double value) {

  public ChartDataDTO(String label, Long value) {
    this(label, value != null ? value.doubleValue() : null);
  }

  public ChartDataDTO(String label, BigDecimal value) {
    this(label, value != null ? value.doubleValue() : null);
  }
}
