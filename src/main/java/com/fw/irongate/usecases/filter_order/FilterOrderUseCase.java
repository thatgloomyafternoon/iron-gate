package com.fw.irongate.usecases.filter_order;

import static com.fw.irongate.constants.MessageConstants.NOT_TIED_TO_WAREHOUSE;

import com.fw.irongate.models.dto.JwtClaimDTO;
import com.fw.irongate.models.dto.OrderDTO;
import com.fw.irongate.models.dto.OrderProductDTO;
import com.fw.irongate.models.entities.Order;
import com.fw.irongate.models.entities.Warehouse;
import com.fw.irongate.models.entities.WarehouseUser;
import com.fw.irongate.repositories.OrderRepository;
import com.fw.irongate.repositories.WarehouseUserRepository;
import com.fw.irongate.repositories.specs.OrderSpecification;
import com.fw.irongate.usecases.UseCase;
import com.fw.irongate.web.responses.PaginatedResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@UseCase
public class FilterOrderUseCase {

  private final OrderRepository orderRepository;
  private final WarehouseUserRepository warehouseUserRepository;

  public FilterOrderUseCase(
      OrderRepository orderRepository, WarehouseUserRepository warehouseUserRepository) {
    this.orderRepository = orderRepository;
    this.warehouseUserRepository = warehouseUserRepository;
  }

  public PaginatedResponse<OrderDTO> handle(JwtClaimDTO jwtClaimDTO, FilterOrderRequest request) {
    List<UUID> warehouseIds =
        warehouseUserRepository.findAllByUserId(jwtClaimDTO.userId()).stream()
            .map(WarehouseUser::getWarehouse)
            .map(Warehouse::getId)
            .toList();
    if (warehouseIds.isEmpty()) {
      throw new IllegalArgumentException(NOT_TIED_TO_WAREHOUSE);
    }
    Pageable pageable =
        PageRequest.of(request.page(), request.size(), Sort.by("createdAt").descending());
    Page<Order> orderPage =
        orderRepository.findAll(
            OrderSpecification.getSpecification(request, warehouseIds), pageable);
    List<OrderDTO> dtos =
        orderPage.getContent().stream()
            .map(
                order -> {
                  List<OrderProductDTO> productDTOs =
                      order.getOrderProducts().stream()
                          .map(
                              op ->
                                  new OrderProductDTO(op.getProduct().getName(), op.getQuantity()))
                          .toList();
                  return new OrderDTO(
                      order.getId(),
                      order.getCustomerName(),
                      productDTOs,
                      order.getTotalPrice(),
                      order.getWarehouse().getName(),
                      order.getStatus(),
                      order.getUpdatedAt(),
                      order.getUpdatedBy());
                })
            .toList();

    return new PaginatedResponse<>(
        dtos, orderPage.getNumber(), orderPage.getTotalElements(), orderPage.getTotalPages());
  }
}
