package com.fw.irongate.web.api;

import com.fw.irongate.models.dto.PermissionDTO;
import com.fw.irongate.usecases.get_all_permissions.GetAllPermissionsUseCase;
import com.fw.irongate.usecases.get_simulation_flag.GetSimulationFlagUseCase;
import com.fw.irongate.usecases.toggle_simulation.ToggleSimulationUseCase;
import com.fw.irongate.web.responses.MessageResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sysconfig")
public class SysconfigController {

  private final GetSimulationFlagUseCase getSimulationFlagUseCase;
  private final ToggleSimulationUseCase toggleSimulationUseCase;
  private final GetAllPermissionsUseCase getAllPermissionsUseCase;

  public SysconfigController(
      GetSimulationFlagUseCase getSimulationFlagUseCase,
      ToggleSimulationUseCase toggleSimulationUseCase,
      GetAllPermissionsUseCase getAllPermissionsUseCase) {
    this.getSimulationFlagUseCase = getSimulationFlagUseCase;
    this.toggleSimulationUseCase = toggleSimulationUseCase;
    this.getAllPermissionsUseCase = getAllPermissionsUseCase;
  }

  @GetMapping("/get-simulation-flag")
  public ResponseEntity<MessageResponse> getSimulationFlag() {
    return ResponseEntity.ok(getSimulationFlagUseCase.handle());
  }

  @PostMapping("/toggle-simulation")
  public ResponseEntity<MessageResponse> toggleSimulation() {
    return ResponseEntity.ok(toggleSimulationUseCase.handle());
  }

  @GetMapping("/list-permissions")
  public ResponseEntity<List<PermissionDTO>> listPermissions() {
    return ResponseEntity.ok(getAllPermissionsUseCase.handle());
  }
}
