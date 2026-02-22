package tn.temporise.application.mapper;

import org.hibernate.Hibernate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import tn.temporise.domain.model.*;
import tn.temporise.infrastructure.persistence.entity.AdresseEntity;
import tn.temporise.infrastructure.persistence.entity.CommandeEntity;

import java.util.Set;
import java.util.stream.Collectors;


@Mapper(componentModel = "spring")
public interface AdresseMapper {
  @Mapping(target = "utilisateur", source = "utilisateurId",
      qualifiedByName = "mapUtilisateurIdToUtilisateur")
  Adresse dtoToModel(AdresseRequest adresseRequest);

  AdresseResponse modelToDto(Adresse adresse);

  AdresseEntity modelToEntity(Adresse adresse);

  @Mapping(target = "commandesLivraison", source = "commandesLivraison",
      qualifiedByName = "mapCommandeEntitiesToCommandes")
  @Mapping(target = "commandesFacturation", source = "commandesFacturation",
      qualifiedByName = "mapCommandeEntitiesToCommandes")
  Adresse entityToModel(AdresseEntity adresseEntity);

  @Named("mapUtilisateurIdToUtilisateur")
  default UtilisateurModel mapUtilisateurIdToUtilisateur(Long id) {
    if (id == null)
      return UtilisateurModel.builder().build();
    return UtilisateurModel.builder().id(id).build();
  }

  @Named("mapCommandeEntitiesToCommandes")
  default Set<Commande> mapCommandeEntitiesToCommandes(Set<CommandeEntity> commandeEntities) {
    if (commandeEntities == null || !Hibernate.isInitialized(commandeEntities))
      return null;
    return commandeEntities.stream()
        .map((c) -> Commande.builder().id(c.getId()).total(c.getTotal()).date(c.getDate()).build())
        .collect(Collectors.toSet());
  }
}
