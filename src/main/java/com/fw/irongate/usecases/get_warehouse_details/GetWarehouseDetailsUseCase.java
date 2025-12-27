package com.fw.irongate.usecases.get_warehouse_details;

import static com.fw.irongate.constants.MessageConstants.WAREHOUSE_NOT_FOUND;

import com.fw.irongate.models.dto.UserDTO;
import com.fw.irongate.models.dto.WarehouseDetailsDTO;
import com.fw.irongate.models.entities.WarehouseUser;
import com.fw.irongate.repositories.WarehouseUserRepository;
import com.fw.irongate.usecases.UseCase;
import java.util.List;
import java.util.UUID;

@UseCase
public class GetWarehouseDetailsUseCase {

  private final WarehouseUserRepository warehouseUserRepository;

  public GetWarehouseDetailsUseCase(WarehouseUserRepository warehouseUserRepository) {
    this.warehouseUserRepository = warehouseUserRepository;
  }

  public WarehouseDetailsDTO handle(UUID warehouseId) {
    List<WarehouseUser> warehouseUsers =
        warehouseUserRepository.findByWarehouseIdWithRelations(warehouseId);
    if (warehouseUsers.isEmpty()) {
      throw new IllegalArgumentException(WAREHOUSE_NOT_FOUND);
    }
    List<UserDTO> users =
        warehouseUsers.stream()
            .map(
                wu ->
                    new UserDTO(
                        wu.getUser().getId(),
                        wu.getUser().getEmail(),
                        wu.getUser().getRole().getValue(),
                        wu.getUser().getFullName()))
            .toList();
    return new WarehouseDetailsDTO(warehouseUsers.getFirst().getWarehouse().getName(), users);
  }
}
