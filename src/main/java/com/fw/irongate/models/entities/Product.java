package com.fw.irongate.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@SuppressWarnings("unused")
@Table(name = "products")
@Entity
public class Product extends BaseEntity {

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "sku", nullable = false, unique = true)
  private String sku;

  @Column(name = "description")
  private String description;

  @Column(name = "price", nullable = false, precision = 19, scale = 2)
  @Positive
  private BigDecimal price;

  @Column(name = "quantity", nullable = false)
  @PositiveOrZero
  private Integer quantity;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSku() {
    return sku;
  }

  public void setSku(String sku) {
    this.sku = sku;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
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
