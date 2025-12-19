package com.fw.irongate.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@SuppressWarnings("unused")
@Table(name = "sysconfig_types")
@Entity
public class SysconfigType extends BaseEntity {

  @Column(name = "name", nullable = false)
  @NotBlank
  private String name;

  @Column(name = "description", nullable = false)
  @NotBlank
  private String description;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
