package com.fw.irongate.models.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@SuppressWarnings("unused")
@Table(name = "permissions")
@Entity
public class Permission extends BaseEntity {

  @JoinColumn(name = "role_id", nullable = false)
  @ManyToOne
  private Sysconfig role;

  @JoinColumn(name = "resource_path_id", nullable = false)
  @ManyToOne
  private Sysconfig resourcePath;

  public Sysconfig getRole() {
    return role;
  }

  public void setRole(Sysconfig role) {
    this.role = role;
  }

  public Sysconfig getResourcePath() {
    return resourcePath;
  }

  public void setResourcePath(Sysconfig resourcePath) {
    this.resourcePath = resourcePath;
  }
}
