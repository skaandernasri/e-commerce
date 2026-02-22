package tn.temporise.application.mapper;

import org.mapstruct.Mapper;
import tn.temporise.domain.model.*;
import tn.temporise.infrastructure.persistence.entity.CodePromoEntity;

@Mapper(componentModel = "spring", uses = {DateMapper.class})
public interface CodePromoMapper {
  CodePromo dtoToModel(CodePromoRequest codePromoRequest);

  CodePromoEntity modelToEntity(CodePromo codePromo);

  CodePromo entityToModel(CodePromoEntity codePromoEntity);

  CodePromoResponse modelToResponse(CodePromo codePromo);

  CodePromoResponse entityToResponse(CodePromoEntity codePromoEntity);

}
