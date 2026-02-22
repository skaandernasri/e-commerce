package tn.temporise.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import tn.temporise.application.mapper.ConfigurationGlobalMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.ConfigurationGlobal;
import tn.temporise.domain.port.ConfigurationGlobalRepo;
import tn.temporise.infrastructure.persistence.entity.ConfigurationGlobalEntity;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ConfigurationGlobalRepoImp implements ConfigurationGlobalRepo {
  private final ConfigurationGlobalMapper configurationGlobalMapper;
  private final ConfigurationGlobalJpaRepo configurationGlobalJpaRepo;
  private final ExceptionFactory exceptionFactory;

  @Override
  public ConfigurationGlobal save(ConfigurationGlobal config) {
    ConfigurationGlobalEntity savedEntity =
        configurationGlobalJpaRepo.save(configurationGlobalMapper.modelToEntity(config));
    return configurationGlobalMapper.entityToModel(savedEntity);
  }

  @Override
  public ConfigurationGlobal getConfig() {
    Optional<ConfigurationGlobalEntity> configEntity = configurationGlobalJpaRepo.findById(1L);
    return configEntity.map(configurationGlobalMapper::entityToModel)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.config"));
  }
}
