package com.fw.irongate.repositories;

import com.fw.irongate.models.entities.WarehouseUser;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

@SuppressWarnings("JavadocDeclaration")
public interface WarehouseUserRepository extends JpaRepository<WarehouseUser, UUID> {

  List<WarehouseUser> findAllByUserId(UUID userId);

  /**
   * The query using an explicit join condition (<code>JOIN FETCH User u ON wu.user.id = u.id</code>) instead
   * of joining the entity's association path (<code>JOIN FETCH wu.user</code>) may cause LazyInitializationException.<br>
   * While this syntax fetches the data, it often fails to correctly initialize the Hibernate proxy for the
   * wu.user association in the returned WarehouseUser entities.<br>
   * Consequently, when wu.getUser() is accessed in GetWarehouseDetailsUseCase (which is outside the transaction
   * scope), the proxy is uninitialized, causing the exception.
   *
   * @param warehouseId
   * @param userId
   * @return
   */
  @Query(
      value =
          "SELECT wu FROM WarehouseUser wu "
              + "JOIN FETCH wu.warehouse w "
              + "JOIN FETCH wu.user u "
              + "WHERE w.id = ?1 AND "
              + "u.id = ?2 AND "
              + "wu.deletedAt IS NULL AND "
              + "w.deletedAt IS NULL AND "
              + "u.deletedAt IS NULL")
  List<WarehouseUser> findByWarehouseIdAndUserId(UUID warehouseId, UUID userId);

  @Query(
      value =
          "SELECT wu FROM WarehouseUser wu "
              + "JOIN FETCH wu.warehouse w "
              + "JOIN FETCH wu.user u "
              + "JOIN FETCH u.role s "
              + "WHERE w.id = ?1 AND "
              + "wu.deletedAt IS NULL AND "
              + "w.deletedAt IS NULL AND "
              + "u.deletedAt IS NULL")
  List<WarehouseUser> findByWarehouseIdWithRelations(UUID warehouseId);
}
