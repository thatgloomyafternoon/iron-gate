package com.fw.irongate.models.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * General Rule:<br>
 * Always default to LAZY for @ManyToOne fetch type.<br>
 * If you get a LazyInitializationException later,<br>
 * it means you forgot to use JOIN FETCH in your repository query.
 */
@SuppressWarnings("unused")
@Table(name = "permissions")
@Entity
public class Permission extends BaseEntity {

  @JoinColumn(name = "role_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Sysconfig role;

  @JoinColumn(name = "resource_path_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
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
