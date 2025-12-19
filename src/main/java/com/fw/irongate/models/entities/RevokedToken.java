package com.fw.irongate.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Persistable;

/**
 * The reason for:<br>
 * - Persistable<UUID><br>
 * - @Transient private boolean isNew = true;<br>
 * - void markNotNew()<br>
 * is to resolve the hidden behavior in Spring Data JPA: "Select Before Insert".<br>
 * Explanation:<br>
 * You are generating the UUID in Java (UuidCreator.getTimeOrderedEpoch()).<br>
 * The Problem: When you pass an object with a non-null ID to .save(), Spring Data assumes it might be an Update, not a new Insert.<br>
 * The Behavior: It calls entityManager.merge(), which fires a SELECT query first to see if that ID already exists in the database. When it finds nothing, then it fires the INSERT.<br>
 * The Fix: You must implement the Persistable interface in your RevokedToken entity to tell Spring "I know I have an ID, but trust me, I am new."
 */
@Table(name = "revoked_tokens")
@Entity
public class RevokedToken extends BaseEntity implements Persistable<UUID> {

  @Transient private boolean isNew = true;

  @Column(name = "jwt", nullable = false, unique = true, length = 1000)
  private String jwt;

  @Column(name = "expired_at", nullable = false)
  private Instant expiredAt;

  @SuppressWarnings("unused")
  public RevokedToken() {}

  public RevokedToken(String jwt, Instant expiredAt) {
    this.jwt = jwt;
    this.expiredAt = expiredAt;
  }

  @Override
  public boolean isNew() {
    return isNew;
  }

  @PostPersist
  @PostLoad
  void markNotNew() {
    this.isNew = false;
  }

  public String getJwt() {
    return jwt;
  }

  public void setJwt(String jwt) {
    this.jwt = jwt;
  }
}
