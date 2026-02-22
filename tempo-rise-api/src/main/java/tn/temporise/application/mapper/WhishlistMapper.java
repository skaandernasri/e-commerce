package tn.temporise.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tn.temporise.domain.model.Whishlist;
import tn.temporise.domain.model.WhishlistDto;
import tn.temporise.infrastructure.persistence.entity.WhishlistEntity;

@Mapper(componentModel = "spring", uses = {DateMapper.class, ProductMapper.class})
public interface WhishlistMapper {
  @Mapping(target = "utilisateurId", source = "utilisateur.id")
  WhishlistDto modelToDto(Whishlist whishlist);

  WhishlistEntity modelToEntity(Whishlist whishlist);

  Whishlist entityToModel(WhishlistEntity whishlistEntity);
  //
  // Whishlist dtoToModel(WhishlistResponse whishlistResponse);
}
