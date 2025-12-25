package com.fw.irongate.simulator;

import static com.fw.irongate.constants.SystemConstants.EVENT_ORDER_CREATED;
import static com.fw.irongate.constants.SystemConstants.SIMULATION_RUN_FLAG;

import com.fw.irongate.models.dto.DashboardEventDTO;
import com.fw.irongate.models.entities.Order;
import com.fw.irongate.models.entities.OrderProduct;
import com.fw.irongate.models.entities.Product;
import com.fw.irongate.models.entities.Sysconfig;
import com.fw.irongate.models.entities.Warehouse;
import com.fw.irongate.models.enums.OrderStatus;
import com.fw.irongate.repositories.OrderProductRepository;
import com.fw.irongate.repositories.OrderRepository;
import com.fw.irongate.repositories.ProductRepository;
import com.fw.irongate.repositories.SysconfigRepository;
import com.fw.irongate.repositories.WarehouseRepository;
import com.fw.irongate.usecases.UseCase;
import com.fw.irongate.usecases.stream_dashboard.StreamDashboardUseCase;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

@UseCase
public class Simulator {

  private final String[] CUSTOMER_NAME = {
    "Yuki Tanaka", "Hiroshi Sato", "Kenji Suzuki",
    "Akiko Takahashi", "Minoru Watanabe", "Haruto Ito",
    "Yua Yamamoto", "Ren Nakamura", "Daiki Shimizu",
    "Nanami Yamazaki", "Kouta Mori", "Rin Abe",
    "Takumi Ikeda", "Hana Hashimoto", "Ryota Yamashita",
    "James Smith", "Emma Johnson", "Robert Williams",
    "Olivia Brown", "Michael Jones", "Sophia Garcia",
    "William Miller", "Isabella Davis", "David Rodriguez",
    "Mia Martinez", "Richard Hernandez", "Charlotte Lopez",
    "Joseph Gonzalez", "Amelia Wilson", "Thomas Anderson"
  };
  private static final Logger log = LoggerFactory.getLogger(Simulator.class);
  private final ProductRepository productRepository;
  private final WarehouseRepository warehouseRepository;
  private final OrderRepository orderRepository;
  private final OrderProductRepository orderProductRepository;
  private final SysconfigRepository sysconfigRepository;
  private final StreamDashboardUseCase streamDashboardUseCase;
  private final Random random;

  public Simulator(
      ProductRepository productRepository,
      WarehouseRepository warehouseRepository,
      OrderRepository orderRepository,
      OrderProductRepository orderProductRepository,
      SysconfigRepository sysconfigRepository,
      StreamDashboardUseCase streamDashboardUseCase) {
    this.productRepository = productRepository;
    this.warehouseRepository = warehouseRepository;
    this.orderRepository = orderRepository;
    this.orderProductRepository = orderProductRepository;
    this.sysconfigRepository = sysconfigRepository;
    this.streamDashboardUseCase = streamDashboardUseCase;
    this.random = new Random();
  }

  @Scheduled(cron = "0 */2 * * * *")
  public void task() {
    Optional<Sysconfig> optSysconfig = sysconfigRepository.findByKey(SIMULATION_RUN_FLAG);
    if (optSysconfig.isEmpty()) {
      return;
    }
    Sysconfig sysconfig = optSysconfig.get();
    if (!sysconfig.getValue().equals("true")) {
      return;
    }
    if (productRepository.findAll().isEmpty()) {
      log.info("No product found");
      return;
    }
    String customerName = getRandomCustomerName();
    createOrder(customerName);
    log.info("Created an order for \"{}\"", customerName);
  }

  private String getRandomCustomerName() {
    return CUSTOMER_NAME[random.nextInt(CUSTOMER_NAME.length)];
  }

  private void createOrder(String customerName) {
    Order order = new Order();
    order.setCreatedBy(customerName);
    order.setUpdatedBy(customerName);
    order.setCustomerName(customerName);
    order.setStatus(OrderStatus.PENDING.name());
    order.setWarehouse(getRandomWarehouse());
    order = orderRepository.save(order);
    int n = random.nextInt(3) + 1;
    List<OrderProduct> orderProducts = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      OrderProduct orderProduct = new OrderProduct();
      orderProduct.setCreatedBy(customerName);
      orderProduct.setUpdatedBy(customerName);
      orderProduct.setProduct(getRandomProduct());
      orderProduct.setQuantity(random.nextInt(10) + 1);
      orderProduct.setPrice(orderProduct.getProduct().getPrice());
      orderProduct.setOrder(order);
      orderProducts.add(orderProduct);
    }
    orderProductRepository.saveAll(orderProducts);
    streamDashboardUseCase.broadcast(new DashboardEventDTO(EVENT_ORDER_CREATED));
  }

  private Product getRandomProduct() {
    List<Product> products = productRepository.findAll();
    return products.get(random.nextInt(products.size()));
  }

  private Warehouse getRandomWarehouse() {
    List<Warehouse> warehouses = warehouseRepository.findAll();
    List<Warehouse> purchasableWarehouse =
        warehouses.stream().filter(w -> !w.getName().equals("Hachioji")).toList();
    return purchasableWarehouse.get(random.nextInt(purchasableWarehouse.size()));
  }
}
