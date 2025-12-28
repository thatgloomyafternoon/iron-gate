package com.fw.irongate.usecases.get_dashboard_charts;

import com.fw.irongate.models.dto.ChartDataDTO;
import com.fw.irongate.models.dto.DashboardChartsDTO;
import com.fw.irongate.repositories.OrderProductRepository;
import com.fw.irongate.repositories.OrderRepository;
import com.fw.irongate.repositories.ShipmentRepository;
import com.fw.irongate.usecases.UseCase;
import java.util.List;
import org.springframework.data.domain.PageRequest;

@UseCase
public class GetDashboardChartsUseCase {

  private final OrderRepository orderRepository;
  private final ShipmentRepository shipmentRepository;
  private final OrderProductRepository orderProductRepository;

  public GetDashboardChartsUseCase(
      OrderRepository orderRepository,
      ShipmentRepository shipmentRepository,
      OrderProductRepository orderProductRepository) {
    this.orderRepository = orderRepository;
    this.shipmentRepository = shipmentRepository;
    this.orderProductRepository = orderProductRepository;
  }

  public DashboardChartsDTO handle() {
    List<ChartDataDTO> ordersByStatus = orderRepository.countOrdersByStatus();
    List<ChartDataDTO> shipmentsByStatus = shipmentRepository.countShipmentsByStatus();
    List<ChartDataDTO> top5RevenueByWarehouse =
        orderRepository.findTopRevenueByWarehouse(PageRequest.of(0, 5));
    List<ChartDataDTO> topSellingProducts =
        orderProductRepository.findTopSellingProducts(PageRequest.of(0, 5));
    return new DashboardChartsDTO(
        ordersByStatus, shipmentsByStatus, top5RevenueByWarehouse, topSellingProducts);
  }
}
