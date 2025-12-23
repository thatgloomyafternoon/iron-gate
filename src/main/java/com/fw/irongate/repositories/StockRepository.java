package com.fw.irongate.repositories;

import com.fw.irongate.models.entities.Stock;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StockRepository
    extends JpaRepository<Stock, UUID>, JpaSpecificationExecutor<Stock> {}
