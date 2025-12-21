package com.fw.irongate.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

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
}
