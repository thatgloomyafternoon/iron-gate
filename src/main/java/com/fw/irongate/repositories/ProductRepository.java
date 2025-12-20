package com.fw.irongate.repositories;

import com.fw.irongate.models.entities.Product;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, UUID> {

  Optional<Product> findBySku(String sku);
}
