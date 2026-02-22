package tn.temporise.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tn.temporise.domain.model.*;
import tn.temporise.infrastructure.persistence.entity.*;


@Mapper(componentModel = "spring", uses = {DateMapper.class, CommandeMapper.class})
public interface FactureMapper {
  @Mapping(target = "commande.id", source = "commandeId")
  Facture dtoToModel(FactureRequest factureRequest);

  FactureEntity modelToEntity(Facture facture);

  Facture entityToModel(FactureEntity factureEntity);

  FactureResponse modelToDto(Facture facture);



}
