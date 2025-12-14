package com.fw.irongate.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@SuppressWarnings("unused")
@MappedSuperclass
public abstract class BaseEntity {

  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  /**
   * NOTE: when using "nullable = false" (as opposed to @NotNull), hibernate will not do any
   * validation to the null value of this field before persisting, instead, the validation will
   * happen on the database level (the 'nullable' attribute of the @Column annotation), and when
   * combined with @CreationTimestamp, hibernate will automatically generate the value and override
   * the content of the field when persisting.
   */
  @Column(name = "created_at", nullable = false)
  @CreationTimestamp
  private ZonedDateTime createdAt;

  @Column(name = "created_by", nullable = false)
  @NotBlank
  private String createdBy;

  @Column(name = "updated_at", nullable = false)
  @UpdateTimestamp
  private ZonedDateTime updatedAt;

  @Column(name = "updated_by", nullable = false)
  @NotBlank
  private String updatedBy;

  @Column(name = "deleted_at")
  private ZonedDateTime deletedAt;

  @Column(name = "deleted_by")
  private String deletedBy;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public ZonedDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(ZonedDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public ZonedDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(ZonedDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  public ZonedDateTime getDeletedAt() {
    return deletedAt;
  }

  public void setDeletedAt(ZonedDateTime deletedAt) {
    this.deletedAt = deletedAt;
  }

  public String getDeletedBy() {
    return deletedBy;
  }

  public void setDeletedBy(String deletedBy) {
    this.deletedBy = deletedBy;
  }
}
