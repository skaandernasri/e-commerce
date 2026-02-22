package tn.temporise.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import tn.temporise.domain.model.Role;
import tn.temporise.domain.model.SignupUserRequest;
import tn.temporise.domain.model.UtilisateurModel;
import tn.temporise.infrastructure.persistence.entity.UtilisateurEntity;


import java.util.Collections;
import java.util.Set;

@Mapper(componentModel = "spring", uses = DateMapper.class)
public interface RegMapper {
  @Mapping(target = "roles", source = "roles")
  @Mapping(target = "userType", expression = "java(tn.temporise.domain.model.UserType.NORMAL)")
  UtilisateurEntity modelToEntity(UtilisateurModel utilisateurModel);

  @Mapping(target = "roles", source = "roles")
  @Mapping(target = "date_naissance", source = "dateNaissance")
  UtilisateurModel toModel(SignupUserRequest signupUserRequest);

  @Named("mapRoleToSet")
  default Set<Role> mapRoleToSet(Role role) {
    return role != null ? Set.of(role) : Collections.emptySet();
  }

  @Mapping(target = "roles", source = "roles")
  UtilisateurModel entityToModel(UtilisateurEntity utilisateurEntity);

}

