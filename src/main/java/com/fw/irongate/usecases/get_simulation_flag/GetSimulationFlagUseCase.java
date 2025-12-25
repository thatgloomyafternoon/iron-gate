package com.fw.irongate.usecases.get_simulation_flag;

import static com.fw.irongate.constants.SystemConstants.ERROR;
import static com.fw.irongate.constants.SystemConstants.SIMULATION_RUN_FLAG;

import com.fw.irongate.models.entities.Sysconfig;
import com.fw.irongate.repositories.SysconfigRepository;
import com.fw.irongate.usecases.UseCase;
import com.fw.irongate.web.responses.MessageResponse;
import java.util.Optional;

@UseCase
public class GetSimulationFlagUseCase {

  private final SysconfigRepository sysconfigRepository;

  public GetSimulationFlagUseCase(SysconfigRepository sysconfigRepository) {
    this.sysconfigRepository = sysconfigRepository;
  }

  public MessageResponse handle() {
    Optional<Sysconfig> optSysconfig = sysconfigRepository.findByKey(SIMULATION_RUN_FLAG);
    if (optSysconfig.isEmpty()) {
      throw new IllegalArgumentException(ERROR);
    }
    Sysconfig sysconfig = optSysconfig.get();
    return new MessageResponse(sysconfig.getValue());
  }
}
