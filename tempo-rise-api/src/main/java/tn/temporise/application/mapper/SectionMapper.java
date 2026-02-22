package tn.temporise.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tn.temporise.domain.model.*;
import tn.temporise.infrastructure.persistence.entity.SectionEntity;

@Mapper(componentModel = "spring", uses = {DateMapper.class, UserMapper.class})
public interface SectionMapper {

  Section dtoToModel(ParamSectionRequest paramSectionRequest);

  SectionEntity modelToEntity(Section section);

  Section entityToModel(SectionEntity sectionEntity);

  @Mapping(target = "createdById", expression = "java(mapToLong(section.createdBy()))")
  @Mapping(target = "updatedById", expression = "java(mapToLong(section.updatedBy()))")
  ParamSectionResponse modelToDto(Section section);

  default UtilisateurModel mapToUtilisateur(Long id) {
    return id != null ? UtilisateurModel.builder().id(id).build() : null;
  }

  default Long mapToLong(UtilisateurModel utilisateurModel) {
    return utilisateurModel != null && utilisateurModel.id() != null ? utilisateurModel.id() : null;
  }

}
