package com.fw.irongate.repositories;

import com.fw.irongate.models.entities.SysconfigType;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

@SuppressWarnings("unused")
public interface SysconfigTypeRepository extends JpaRepository<SysconfigType, UUID> {}
