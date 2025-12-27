package com.fw.irongate.usecases.get_dashboard_charts;

import com.fw.irongate.models.dto.ChartDataDTO;
import com.fw.irongate.models.dto.DashboardChartsDTO;
import com.fw.irongate.repositories.OrderProductRepository;
import com.fw.irongate.repositories.OrderRepository;
import com.fw.irongate.repositories.ShipmentRepository;
import com.fw.irongate.repositories.StockRepository;
import com.fw.irongate.usecases.UseCase;
import java.util.List;
import org.springframework.data.domain.PageRequest;

@UseCase
public class GetDashboardChartsUseCase {

  private final OrderRepository orderRepository;
  private final ShipmentRepository shipmentRepository;
  private final StockRepository stockRepository;
  private final OrderProductRepository orderProductRepository;

  public GetDashboardChartsUseCase(
      OrderRepository orderRepository,
      ShipmentRepository shipmentRepository,
      StockRepository stockRepository,
      OrderProductRepository orderProductRepository) {
    this.orderRepository = orderRepository;
    this.shipmentRepository = shipmentRepository;
    this.stockRepository = stockRepository;
    this.orderProductRepository = orderProductRepository;
  }

  public DashboardChartsDTO handle() {
    List<ChartDataDTO> ordersByStatus = orderRepository.countOrdersByStatus();
    List<ChartDataDTO> shipmentsByStatus = shipmentRepository.countShipmentsByStatus();
    List<ChartDataDTO> inventoryByWarehouse = stockRepository.sumQuantityByWarehouse();
    List<ChartDataDTO> topSellingProducts =
        orderProductRepository.findTopSellingProducts(PageRequest.of(0, 5));
    return new DashboardChartsDTO(
        ordersByStatus, shipmentsByStatus, inventoryByWarehouse, topSellingProducts);
  }
}
