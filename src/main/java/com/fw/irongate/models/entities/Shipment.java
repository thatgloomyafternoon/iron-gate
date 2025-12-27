package com.fw.irongate.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Table(name = "shipments")
@Entity
public class Shipment extends BaseEntity {

  @JoinColumn(name = "stock_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Stock stock;

  @JoinColumn(name = "warehouse_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Warehouse destWarehouse;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Column(name = "status", nullable = false)
  @NotBlank
  private String status;

  @Column(name = "code", nullable = false)
  private String code;

  @Column(name = "assigned_to")
  private String assignedTo;

  public Stock getStock() {
    return stock;
  }

  public void setStock(Stock stock) {
    this.stock = stock;
  }

  public Warehouse getDestWarehouse() {
    return destWarehouse;
  }

  public void setDestWarehouse(Warehouse destWarehouse) {
    this.destWarehouse = destWarehouse;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getAssignedTo() {
    return assignedTo;
  }

  public void setAssignedTo(String assignedTo) {
    this.assignedTo = assignedTo;
  }
}
