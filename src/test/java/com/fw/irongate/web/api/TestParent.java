package com.fw.irongate.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fw.irongate.models.entities.Order;
import com.fw.irongate.models.entities.OrderProduct;
import com.fw.irongate.models.entities.Permission;
import com.fw.irongate.models.entities.Product;
import com.fw.irongate.models.entities.Shipment;
import com.fw.irongate.models.entities.Stock;
import com.fw.irongate.models.entities.Sysconfig;
import com.fw.irongate.models.entities.SysconfigType;
import com.fw.irongate.models.entities.User;
import com.fw.irongate.models.entities.Warehouse;
import com.fw.irongate.models.entities.WarehouseUser;
import com.fw.irongate.repositories.OrderProductRepository;
import com.fw.irongate.repositories.OrderRepository;
import com.fw.irongate.repositories.PermissionRepository;
import com.fw.irongate.repositories.ProductRepository;
import com.fw.irongate.repositories.RevokedTokenRepository;
import com.fw.irongate.repositories.ShipmentRepository;
import com.fw.irongate.repositories.StockRepository;
import com.fw.irongate.repositories.SysconfigRepository;
import com.fw.irongate.repositories.SysconfigTypeRepository;
import com.fw.irongate.repositories.UserRepository;
import com.fw.irongate.repositories.WarehouseRepository;
import com.fw.irongate.repositories.WarehouseUserRepository;
import com.fw.irongate.utils.JwtUtil;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SuppressWarnings({"SameParameterValue", "UnusedReturnValue"})
public abstract class TestParent {

  @Autowired protected SysconfigTypeRepository sysconfigTypeRepository;
  @Autowired protected SysconfigRepository sysconfigRepository;
  @Autowired protected UserRepository userRepository;
  @Autowired protected PermissionRepository permissionRepository;
  @Autowired protected RevokedTokenRepository revokedTokenRepository;
  @Autowired protected ProductRepository productRepository;
  @Autowired protected WarehouseRepository warehouseRepository;
  @Autowired protected StockRepository stockRepository;
  @Autowired protected WarehouseUserRepository warehouseUserRepository;
  @Autowired protected ShipmentRepository shipmentRepository;
  @Autowired protected OrderRepository orderRepository;
  @Autowired protected OrderProductRepository orderProductRepository;
  @Autowired protected JwtUtil jwtUtil;

  @Autowired protected MockMvc mockMvc;
  @Autowired protected BCryptPasswordEncoder bCryptPasswordEncoder;
  @Autowired protected ObjectMapper objectMapper;

  protected void deleteAll() {
    shipmentRepository.deleteAll();
    orderProductRepository.deleteAll();
    orderRepository.deleteAll();
    stockRepository.deleteAll();
    productRepository.deleteAll();
    warehouseUserRepository.deleteAll();
    warehouseRepository.deleteAll();
    revokedTokenRepository.deleteAll();
    permissionRepository.deleteAll();
    userRepository.deleteAll();
    sysconfigRepository.deleteAll();
    sysconfigTypeRepository.deleteAll();
  }

  protected SysconfigType createSysconfigType(String name, String description) {
    SysconfigType st = new SysconfigType();
    st.setCreatedBy("system");
    st.setUpdatedBy("system");
    st.setName(name);
    st.setDescription(description);
    return sysconfigTypeRepository.save(st);
  }

  protected Sysconfig createSysconfig(SysconfigType sysconfigType, String key, String value) {
    Sysconfig sysconfig = new Sysconfig();
    sysconfig.setCreatedBy("system");
    sysconfig.setUpdatedBy("system");
    sysconfig.setSysconfigType(sysconfigType);
    sysconfig.setKey(key);
    sysconfig.setValue(value);
    return sysconfigRepository.save(sysconfig);
  }

  protected User createUser(Sysconfig role, String email, String passwordHash, String fullName) {
    User user = new User();
    user.setCreatedBy("system");
    user.setUpdatedBy("system");
    user.setRole(role);
    user.setEmail(email);
    user.setPasswordHash(passwordHash);
    user.setFullName(fullName);
    return userRepository.save(user);
  }

  protected Permission createPermission(Sysconfig role, Sysconfig resourcePath) {
    Permission permission = new Permission();
    permission.setCreatedBy("system");
    permission.setUpdatedBy("system");
    permission.setResourcePath(resourcePath);
    permission.setRole(role);
    return permissionRepository.save(permission);
  }

  protected Product createProduct(String name, String sku, String description, BigDecimal price) {
    Product product = new Product();
    product.setCreatedBy("system");
    product.setUpdatedBy("system");
    product.setName(name);
    product.setSku(sku);
    product.setDescription(description);
    product.setPrice(price);
    return productRepository.save(product);
  }

  protected Warehouse createWarehouse(String name, String code) {
    Warehouse warehouse = new Warehouse();
    warehouse.setCreatedBy("system");
    warehouse.setUpdatedBy("system");
    warehouse.setName(name);
    warehouse.setCode(code);
    return warehouseRepository.save(warehouse);
  }

  protected Stock createStock(
      Warehouse warehouse, Product product, Integer quantity, Integer allocated) {
    Stock stock = new Stock();
    stock.setCreatedBy("system");
    stock.setUpdatedBy("system");
    stock.setWarehouse(warehouse);
    stock.setProduct(product);
    stock.setQuantity(quantity);
    stock.setAllocated(allocated);
    return stockRepository.save(stock);
  }

  protected Shipment createShipment(
      Stock stock,
      Warehouse destWarehouse,
      Integer quantity,
      String status,
      String code,
      String assignedTo) {
    Shipment shipment = new Shipment();
    shipment.setCreatedBy("system");
    shipment.setUpdatedBy("system");
    shipment.setStock(stock);
    shipment.setDestWarehouse(destWarehouse);
    shipment.setQuantity(quantity);
    shipment.setStatus(status);
    shipment.setCode(code);
    shipment.setAssignedTo(assignedTo);
    return shipmentRepository.save(shipment);
  }

  protected WarehouseUser createWarehouseUser(Warehouse warehouse, User user) {
    WarehouseUser wu = new WarehouseUser();
    wu.setCreatedBy("system");
    wu.setUpdatedBy("system");
    wu.setWarehouse(warehouse);
    wu.setUser(user);
    return warehouseUserRepository.save(wu);
  }

  protected Order createOrder(
      Warehouse warehouse, String customerName, String status, BigDecimal totalPrice) {
    Order order = new Order();
    order.setCreatedBy("system");
    order.setUpdatedBy("system");
    order.setWarehouse(warehouse);
    order.setCustomerName(customerName);
    order.setStatus(status);
    order.setTotalPrice(totalPrice);
    return orderRepository.save(order);
  }

  protected OrderProduct createOrderProduct(
      Order order, Product product, Integer quantity, BigDecimal price) {
    OrderProduct op = new OrderProduct();
    op.setCreatedBy("system");
    op.setUpdatedBy("system");
    op.setOrder(order);
    op.setProduct(product);
    op.setQuantity(quantity);
    op.setPrice(price);
    return orderProductRepository.save(op);
  }
}
