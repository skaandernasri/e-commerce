package tn.temporise.application.mapper;

import org.mapstruct.Mapper;
import tn.temporise.domain.model.ConfigurationGlobal;
import tn.temporise.domain.model.ConfigurationGlobalRequest;
import tn.temporise.domain.model.ConfigurationGlobalResponse;
import tn.temporise.infrastructure.persistence.entity.ConfigurationGlobalEntity;

@Mapper(componentModel = "spring")
public interface ConfigurationGlobalMapper {
  ConfigurationGlobal dtoToModel(ConfigurationGlobalRequest configurationGlobal);

  ConfigurationGlobalResponse modelToDto(ConfigurationGlobal configurationGlobal);

  ConfigurationGlobalEntity modelToEntity(ConfigurationGlobal configurationGlobal);

  ConfigurationGlobal entityToModel(ConfigurationGlobalEntity configurationGlobal);
}
