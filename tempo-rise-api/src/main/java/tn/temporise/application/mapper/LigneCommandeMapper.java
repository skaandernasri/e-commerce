package tn.temporise.application.mapper;

import org.mapstruct.Mapper;
import tn.temporise.domain.model.LigneCommande;
import tn.temporise.infrastructure.persistence.entity.LigneCommandeEntity;

@Mapper(componentModel = "spring", uses = {CommandeMapper.class})
public interface LigneCommandeMapper {
  LigneCommande entityToModel(LigneCommandeEntity ligneCommandeEntity);

  LigneCommandeEntity modelToEntity(LigneCommande ligneCommande);



}
