package tn.temporise.application.mapper;

import org.mapstruct.Mapper;
import tn.temporise.domain.model.WhishlistItem;
import tn.temporise.infrastructure.persistence.entity.WhishlistItemEntity;

@Mapper(componentModel = "spring", uses = {DateMapper.class, ProductMapper.class})
public interface WhishlistItemMapper {
  WhishlistItemEntity modelToEntity(WhishlistItem whishlistItem);

  WhishlistItem entityToModel(WhishlistItemEntity whishlistEntity);
}
