package tn.temporise.application.mapper;

import org.mapstruct.Mapper;
import tn.temporise.domain.model.RetourProduit;
import tn.temporise.infrastructure.persistence.entity.RetourProduitEntity;

@Mapper(componentModel = "spring", uses = {DateMapper.class, ProductMapper.class, UserMapper.class})
public interface RetourProduitMapper {
  RetourProduitEntity retourProduitToRetourProduitEntity(RetourProduit retourProduit);

  RetourProduit retourProduitEntityToRetourProduit(RetourProduitEntity retourProduitEntity);
}
