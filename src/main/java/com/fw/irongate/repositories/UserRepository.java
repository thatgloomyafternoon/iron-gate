package com.fw.irongate.repositories;

import com.fw.irongate.models.entities.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, UUID> {

  @Query("SELECT u FROM User u WHERE u.email = ?1 AND u.deletedAt IS NULL")
  Optional<User> findOneActiveByEmail(String email);
}
