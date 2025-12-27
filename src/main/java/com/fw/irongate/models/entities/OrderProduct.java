package com.fw.irongate.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Table(name = "orders_products")
@Entity
public class OrderProduct extends BaseEntity {

  @JoinColumn(name = "order_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Order order;

  @JoinColumn(name = "product_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Product product;

  @Column(name = "price", nullable = false, precision = 19, scale = 2)
  @Positive
  private BigDecimal price;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  public Order getOrder() {
    return order;
  }

  public void setOrder(Order order) {
    this.order = order;
  }

  public Product getProduct() {
    return product;
  }

  public void setProduct(Product product) {
    this.product = product;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }
}
