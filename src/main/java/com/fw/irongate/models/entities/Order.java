package com.fw.irongate.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

@SuppressWarnings("unused")
@Table(name = "orders")
@Entity
public class Order extends BaseEntity {

  @JoinColumn(name = "warehouse_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Warehouse warehouse;

  @Column(name = "customer_name", nullable = false)
  @NotBlank
  private String customerName;

  @Column(name = "status", nullable = false)
  @NotBlank
  private String status;

  @Column(name = "total_price", nullable = false, precision = 22, scale = 2)
  @Positive
  private BigDecimal totalPrice;

  @OneToMany(mappedBy = "order")
  private List<OrderProduct> orderProducts;

  public Warehouse getWarehouse() {
    return warehouse;
  }

  public void setWarehouse(Warehouse warehouse) {
    this.warehouse = warehouse;
  }

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public BigDecimal getTotalPrice() {
    return totalPrice;
  }

  public void setTotalPrice(BigDecimal totalPrice) {
    this.totalPrice = totalPrice;
  }

  public List<OrderProduct> getOrderProducts() {
    return orderProducts;
  }

  public void setOrderProducts(List<OrderProduct> orderProducts) {
    this.orderProducts = orderProducts;
  }
}
