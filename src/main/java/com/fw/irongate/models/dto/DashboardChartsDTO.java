package com.fw.irongate.models.dto;

import java.util.List;

public record DashboardChartsDTO(
    List<ChartDataDTO> ordersByStatus,
    List<ChartDataDTO> shipmentsByStatus,
    List<ChartDataDTO> inventoryByWarehouse,
    List<ChartDataDTO> topSellingProducts) {}
