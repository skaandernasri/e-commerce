package tn.temporise.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tn.temporise.domain.model.*;
import tn.temporise.infrastructure.persistence.entity.PromotionEntity;



@Mapper(componentModel = "spring", uses = {DateMapper.class, ProductMapper.class})
public interface PromotionMapper {
  @Mapping(target = "produit.id", source = "produitId")
  Promotion dtoToModel(PromotionRequest promotionRequest);

  PromotionEntity modelToEntity(Promotion promotion);

  Promotion entityToModel(PromotionEntity promotionEntity);

  PromotionResponse modelToResponse(Promotion promotion);

  PromotionResponse entityToResponse(PromotionEntity promotionEntity);



}
