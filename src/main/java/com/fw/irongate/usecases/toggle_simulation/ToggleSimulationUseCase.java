package com.fw.irongate.usecases.toggle_simulation;

import static com.fw.irongate.constants.SystemConstants.ERROR;
import static com.fw.irongate.constants.SystemConstants.OK;
import static com.fw.irongate.constants.SystemConstants.SIMULATION_RUN_FLAG;

import com.fw.irongate.models.entities.Sysconfig;
import com.fw.irongate.repositories.SysconfigRepository;
import com.fw.irongate.usecases.UseCase;
import com.fw.irongate.web.responses.MessageResponse;
import java.util.Optional;

@UseCase
public class ToggleSimulationUseCase {

  private final SysconfigRepository sysconfigRepository;

  public ToggleSimulationUseCase(SysconfigRepository sysconfigRepository) {
    this.sysconfigRepository = sysconfigRepository;
  }

  public MessageResponse handle() {
    Optional<Sysconfig> optSysconfig = sysconfigRepository.findByKey(SIMULATION_RUN_FLAG);
    if (optSysconfig.isEmpty()) {
      return new MessageResponse(ERROR);
    }
    Sysconfig sysconfig = optSysconfig.get();
    String value = sysconfig.getValue().equals("true") ? "false" : "true";
    sysconfig.setValue(value);
    sysconfigRepository.save(sysconfig);
    return new MessageResponse(OK);
  }
}
