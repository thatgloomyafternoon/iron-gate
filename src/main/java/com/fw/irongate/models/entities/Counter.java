package com.fw.irongate.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Table(name = "counters")
@Entity
public class Counter extends BaseEntity {

  @Column(name = "next", nullable = false)
  private Integer next;
}
