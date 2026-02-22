package tn.temporise.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import tn.temporise.domain.model.SuiviClient;
import tn.temporise.domain.model.SuiviClientRequest;
import tn.temporise.domain.model.UtilisateurAnonyme;
import tn.temporise.domain.model.UtilisateurModel;
import tn.temporise.infrastructure.persistence.entity.SuiviClientEntity;

import java.util.UUID;


@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface SuiviClientMapper {
  @Mapping(target = "utilisateur", source = "utilisateurId", qualifiedByName = "mapUtilisateurId")
  @Mapping(target = "produit.id", source = "produitId")
  @Mapping(target = "utilisateurAnonyme", source = "utilisateurAnonymeUuid",
      qualifiedByName = "mapSessionId")
  SuiviClient dtoToModel(SuiviClientRequest suiviClientRequest);

  SuiviClientEntity modelToEntity(SuiviClient suiviClient);

  SuiviClient entityToModel(SuiviClientEntity suiviClientEntity);

  @Named("mapUtilisateurId")
  default UtilisateurModel mapUtilisateurId(Long utilisateurId) {
    if (utilisateurId == null) {
      return null;
    }
    return UtilisateurModel.builder().id(utilisateurId).build();
  }

  @Named("mapSessionId")
  default UtilisateurAnonyme mapSessionId(UUID uuid) {
    if (uuid == null) {
      return null;
    }
    return UtilisateurAnonyme.builder().sessionToken(uuid).build();
  }

}
