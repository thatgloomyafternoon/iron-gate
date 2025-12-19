package com.fw.irongate.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@SuppressWarnings("unused")
@Table(name = "users")
@Entity
public class User extends BaseEntity {

  @Column(name = "email", nullable = false, unique = true)
  @NotBlank
  private String email;

  @Column(name = "password_hash")
  private String passwordHash;

  @JoinColumn(name = "role_id", nullable = false)
  @ManyToOne
  @NotNull
  private Sysconfig role;

  @Column(name = "full_name", nullable = false)
  @NotBlank
  private String fullName;

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public Sysconfig getRole() {
    return role;
  }

  public void setRole(Sysconfig role) {
    this.role = role;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }
}
