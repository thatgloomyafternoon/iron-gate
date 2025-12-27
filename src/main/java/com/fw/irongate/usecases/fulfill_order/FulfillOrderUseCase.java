package com.fw.irongate.usecases.fulfill_order;

import static com.fw.irongate.constants.MessageConstants.OPERATION_NOT_PERMITTED;
import static com.fw.irongate.constants.MessageConstants.ORDER_NOT_FOUND;
import static com.fw.irongate.constants.MessageConstants.PRODUCT_QUANTITY_REQUIREMENT_NOT_FULFILLED;
import static com.fw.irongate.constants.SystemConstants.EVENT_ORDER_UPDATED;
import static com.fw.irongate.constants.SystemConstants.OK;

import com.fw.irongate.models.dto.DashboardEventDTO;
import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.models.entities.Order;
import com.fw.irongate.models.entities.Stock;
import com.fw.irongate.models.entities.WarehouseUser;
import com.fw.irongate.models.enums.OrderStatus;
import com.fw.irongate.repositories.OrderRepository;
import com.fw.irongate.repositories.StockRepository;
import com.fw.irongate.repositories.WarehouseUserRepository;
import com.fw.irongate.usecases.UseCase;
import com.fw.irongate.usecases.stream_dashboard.StreamDashboardUseCase;
import com.fw.irongate.web.responses.MessageResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@UseCase
public class FulfillOrderUseCase {

  private final OrderRepository orderRepository;
  private final WarehouseUserRepository warehouseUserRepository;
  private final StockRepository stockRepository;
  private final StreamDashboardUseCase streamDashboardUseCase;

  public FulfillOrderUseCase(
      OrderRepository orderRepository,
      WarehouseUserRepository warehouseUserRepository,
      StockRepository stockRepository,
      StreamDashboardUseCase streamDashboardUseCase) {
    this.orderRepository = orderRepository;
    this.warehouseUserRepository = warehouseUserRepository;
    this.stockRepository = stockRepository;
    this.streamDashboardUseCase = streamDashboardUseCase;
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  @Transactional
  public MessageResponse handle(JwtClaimDTO jwtClaimDTO, UUID orderId) {
    /* check if the order exists */
    Optional<Order> optOrder = orderRepository.findByIdWithRelations(orderId);
    if (optOrder.isEmpty()) {
      throw new IllegalArgumentException(ORDER_NOT_FOUND);
    }
    Order order = optOrder.get();
    /* check if the status of the order */
    /* is still pending */
    if (!order.getStatus().equals(OrderStatus.PENDING.name())) {
      throw new IllegalArgumentException(OPERATION_NOT_PERMITTED);
    }
    /* check if the user fulfilling the order tied */
    /* to the warehouse related to the order */
    List<WarehouseUser> warehouseUsers =
        warehouseUserRepository.findByWarehouseIdAndUserId(
            order.getWarehouse().getId(), jwtClaimDTO.userId());
    if (warehouseUsers.isEmpty()) {
      throw new IllegalArgumentException(OPERATION_NOT_PERMITTED);
    }
    /* check if the quantity in stock suffice */
    List<Stock> stocks = stockRepository.findAllByWarehouseId(order.getWarehouse().getId());
    order
        .getOrderProducts()
        .forEach(
            op -> {
              Optional<Stock> optStock =
                  stocks.stream()
                      .filter(s -> s.getProduct().getId().equals(op.getProduct().getId()))
                      .findAny();
              if (optStock.isEmpty()) {
                throw new IllegalArgumentException(PRODUCT_QUANTITY_REQUIREMENT_NOT_FULFILLED);
              } else if (optStock.get().getQuantity() < op.getQuantity()) {
                throw new IllegalArgumentException(PRODUCT_QUANTITY_REQUIREMENT_NOT_FULFILLED);
              }
            });
    /* the quantity requirement is fulfilled */
    /* continue to the actual fulfillment */
    List<Stock> updatedStocks = new ArrayList<>();
    order
        .getOrderProducts()
        .forEach(
            op -> {
              Stock stock =
                  stocks.stream()
                      .filter(s -> s.getProduct().getId().equals(op.getProduct().getId()))
                      .findAny()
                      .get();
              stock.setUpdatedBy(jwtClaimDTO.email());
              stock.setQuantity(stock.getQuantity() - op.getQuantity());
              updatedStocks.add(stock);
            });
    stockRepository.saveAll(updatedStocks);
    order.setUpdatedBy(jwtClaimDTO.email());
    order.setStatus(OrderStatus.COMPLETED.name());
    orderRepository.save(order);
    /* send event to frontend */
    streamDashboardUseCase.broadcast(new DashboardEventDTO(EVENT_ORDER_UPDATED));
    return new MessageResponse(OK);
  }
}
