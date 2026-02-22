package tn.temporise.application.mapper;

import org.mapstruct.Mapper;
import tn.temporise.domain.model.*;
import tn.temporise.infrastructure.persistence.entity.CategorieEntity;


@Mapper(componentModel = "spring")
public interface CategorieMapper {
  Categorie dtoToModel(CategorieRequest categorieRequest);

  CategorieEntity modelToEntity(Categorie categorie);

  Categorie entityToModel(CategorieEntity categorie);

  CategorieResponse modelToResponse(Categorie categorie);

  CategorieResponse entityToResponse(CategorieEntity categorie);

}
