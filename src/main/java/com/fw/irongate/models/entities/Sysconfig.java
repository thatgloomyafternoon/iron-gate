package com.fw.irongate.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@SuppressWarnings("unused")
@Table(name = "sysconfigs")
@Entity
public class Sysconfig extends BaseEntity {

  @JoinColumn(name = "sysconfig_type_id", nullable = false)
  @ManyToOne
  private SysconfigType sysconfigType;

  @Column(name = "key", nullable = false)
  @NotBlank
  private String key;

  @Column(name = "value", nullable = false)
  @NotBlank
  private String value;

  public SysconfigType getSysconfigType() {
    return sysconfigType;
  }

  public void setSysconfigType(SysconfigType sysconfigType) {
    this.sysconfigType = sysconfigType;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
