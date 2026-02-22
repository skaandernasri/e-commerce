package tn.temporise.application.mapper;

import org.mapstruct.Mapper;
import tn.temporise.domain.model.UtilisateurAnonyme;
import tn.temporise.domain.model.UtilisateurAnonymeRequest;
import tn.temporise.domain.model.UtilisateurAnonymeResponse;
import tn.temporise.infrastructure.persistence.entity.UtilisateurAnonymeEntity;

@Mapper(componentModel = "spring", uses = {DateMapper.class})
public interface UtilisateurAnonymeMapper {
  UtilisateurAnonyme dtoToModel(UtilisateurAnonymeRequest utilisateurAnonymeRequest);

  UtilisateurAnonymeEntity modelToEntity(UtilisateurAnonyme utilisateurAnonyme);

  UtilisateurAnonyme entityToModel(UtilisateurAnonymeEntity utilisateurAnonymeEntity);

  UtilisateurAnonymeResponse modelToDto(UtilisateurAnonyme utilisateurAnonyme);

}
