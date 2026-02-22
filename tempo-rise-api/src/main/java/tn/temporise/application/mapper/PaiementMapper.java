package tn.temporise.application.mapper;

import org.mapstruct.Mapper;
import tn.temporise.domain.model.Commande;
import tn.temporise.domain.model.Paiement;
import tn.temporise.infrastructure.persistence.entity.CommandeEntity;
import tn.temporise.infrastructure.persistence.entity.PaiementEntity;

@Mapper(componentModel = "spring", uses = {DateMapper.class})
public interface PaiementMapper {
  Paiement entityToModel(PaiementEntity paiementEntity);

  PaiementEntity modelToEntity(Paiement paiement);

  default Commande entityToModel(CommandeEntity commandeEntity) {
    if (commandeEntity == null)
      return null;
    return Commande.builder().id(commandeEntity.getId())
        .user(UserMapper.INSTANCE.entityToModel(commandeEntity.getUser())).build();
  }
}
