package com.fw.irongate.repositories;

import com.fw.irongate.models.entities.RevokedToken;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, UUID> {

  boolean existsByJwt(String jwt);
}
