package tn.temporise.application.mapper;

import org.mapstruct.Mapper;

import org.mapstruct.Mapping;

import tn.temporise.domain.model.*;

import tn.temporise.infrastructure.persistence.entity.*;



@Mapper(componentModel = "spring", uses = {ProductMapper.class, DateMapper.class})
public interface PanierProduitMapper {


  @Mapping(target = "id.panier_id", source = "panier.id")
  @Mapping(target = "id.variant_id", source = "variant.id")
  PanierVariantEntity modelToEntity(PanierVariant panierVariant);


  PanierVariant entityToModel(PanierVariantEntity entity);


}
