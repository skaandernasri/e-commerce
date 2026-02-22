package tn.temporise.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.mapper.ConfigurationGlobalMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.ConfigurationGlobal;
import tn.temporise.domain.model.ConfigurationGlobalRequest;
import tn.temporise.domain.model.ConfigurationGlobalResponse;
import tn.temporise.domain.port.ConfigurationGlobalRepo;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigurationGlobalService {
  private final ConfigurationGlobalRepo configurationGlobalRepo;
  private final ExceptionFactory exceptionFactory;
  private final ConfigurationGlobalMapper configurationGlobalMapper;

  public ConfigurationGlobalResponse saveConfig(
      ConfigurationGlobalRequest configurationGlobalRequest) {
    try {
      if (configurationGlobalRequest == null)
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      ConfigurationGlobal configurationGlobal =
          configurationGlobalMapper.dtoToModel(configurationGlobalRequest);
      configurationGlobal = configurationGlobal.toBuilder().id(1L).build();
      configurationGlobal = configurationGlobalRepo.save(configurationGlobal);
      return configurationGlobalMapper.modelToDto(configurationGlobal);
    } catch (BadRequestException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error",
          "Config creation failed" + e.getMessage());
    }

  }

  public ConfigurationGlobalResponse getConfig() {
    try {
      return configurationGlobalMapper.modelToDto(configurationGlobalRepo.getConfig());
    } catch (NotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error",
          "Config creation failed" + e.getMessage());
    }

  }
}
