package com.fw.irongate.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.PositiveOrZero;

@SuppressWarnings("unused")
@Table(name = "stocks")
@Entity
public class Stock extends BaseEntity {

  @JoinColumn(name = "warehouse_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Warehouse warehouse;

  @JoinColumn(name = "product_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Product product;

  @Column(name = "quantity", nullable = false)
  @PositiveOrZero
  private Integer quantity;

  public Warehouse getWarehouse() {
    return warehouse;
  }

  public void setWarehouse(Warehouse warehouse) {
    this.warehouse = warehouse;
  }

  public Product getProduct() {
    return product;
  }

  public void setProduct(Product product) {
    this.product = product;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }
}
