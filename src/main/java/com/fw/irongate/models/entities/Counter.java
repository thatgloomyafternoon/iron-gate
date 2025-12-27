package com.fw.irongate.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@SuppressWarnings("unused")
@Table(name = "counters")
@Entity
public class Counter extends BaseEntity {

  @Column(name = "next", nullable = false)
  private Integer next;

  public Integer getNext() {
    return next;
  }

  public void setNext(Integer next) {
    this.next = next;
  }
}
