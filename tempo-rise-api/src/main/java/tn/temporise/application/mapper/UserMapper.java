package tn.temporise.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import tn.temporise.domain.model.UserRequest;
import tn.temporise.domain.model.UserResponse;
import tn.temporise.domain.model.UtilisateurModel;
import tn.temporise.infrastructure.persistence.entity.UtilisateurEntity;



@Mapper(componentModel = "spring", uses = DateMapper.class)
public interface UserMapper {
  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  @Mapping(target = "roles", source = "roles")
  @Mapping(target = "userType", expression = "java(tn.temporise.domain.model.UserType.NORMAL)")
  UtilisateurEntity modelToEntity(UtilisateurModel utilisateurModel);

  @Mapping(target = "roles", source = "roles")
  @Mapping(target = "date_naissance", source = "dateNaissance")
  UtilisateurModel toModel(UserRequest userRequest);

  @Named("mapRoleToSet")
  @Mapping(target = "roles", source = "roles")
  UtilisateurModel entityToModel(UtilisateurEntity utilisateurEntity);

  @Mapping(target = "dateNaissance", source = "date_naissance")
  @Mapping(target = "loyaltyGroup", source = "loyalty_group")
  UserResponse modelToResponse(UtilisateurModel utilisateurModel);

}
