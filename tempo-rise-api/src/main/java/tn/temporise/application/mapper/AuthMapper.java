package tn.temporise.application.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tn.temporise.domain.model.Authentification;
import tn.temporise.domain.model.SigninUserRequest;
import tn.temporise.domain.model.UtilisateurModel;
import tn.temporise.infrastructure.persistence.entity.AuthentificationEntity;

@Mapper(componentModel = "spring")
public interface AuthMapper {

  UtilisateurModel toModel(SigninUserRequest signinUserRequest);

  @Mapping(target = "refreshToken", source = "token")
  AuthentificationEntity modelToEntity(Authentification authentification);

  @Mapping(target = "token", source = "refreshToken")
  Authentification entityToModel(AuthentificationEntity authentification);
}
