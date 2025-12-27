package com.fw.irongate.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Table(name = "warehouses")
@Entity
public class Warehouse extends BaseEntity {

  @Column(name = "name", nullable = false, unique = true)
  @NotBlank
  private String name;

  @Column(name = "code", nullable = false)
  @NotBlank
  private String code;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }
}
