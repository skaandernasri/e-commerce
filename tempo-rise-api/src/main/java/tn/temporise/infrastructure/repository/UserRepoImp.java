package tn.temporise.infrastructure.repository;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import tn.temporise.application.mapper.UserMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.Role;
import tn.temporise.domain.model.UserType;
import tn.temporise.domain.model.UtilisateurModel;
import tn.temporise.domain.port.UserRepo;
import tn.temporise.infrastructure.persistence.entity.UtilisateurEntity;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Repository
public class UserRepoImp implements UserRepo {
  private final UserJpaRepo userJpaRepo;
  private final ExceptionFactory exceptionFactory;
  private final UserMapper userMapper;

  @Override
  public UtilisateurModel findByEmail(String email) {
    UtilisateurEntity utilisateurEntity = userJpaRepo.findByEmail(email, UserType.NORMAL)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.user"));
    return userMapper.entityToModel(utilisateurEntity);
  }

  @Override
  public Set<Role> findRolesById(long id) {
    if (userJpaRepo.findById(id, UserType.NORMAL).isEmpty())
      throw exceptionFactory.notFound("notfound.user");
    return userJpaRepo.findRolesById(id, UserType.NORMAL); // Calls the method on the injected
                                                           // repository
  }

  @Override
  public void deleteByEmail(String email) {
    if (userJpaRepo.findByEmail(email, UserType.NORMAL).isEmpty())
      throw exceptionFactory.notFound("notfound.user");
    userJpaRepo.deleteByEmail(email, UserType.NORMAL); // Calls the method on the injected
                                                       // repository
  }

  @Override
  public UtilisateurModel save(UtilisateurModel utilisateurModel) {
    log.info("userModel in save: {}", utilisateurModel);
    UtilisateurEntity utilisateurEntity = userMapper.modelToEntity(utilisateurModel);
    log.info("userEntity in save: {}", utilisateurEntity);
    userJpaRepo.save(utilisateurEntity);
    return userMapper.entityToModel(utilisateurEntity);
  }

  @Override
  public UtilisateurModel findById(Long id) {
    UtilisateurEntity utilisateurEntity = userJpaRepo.findById(id, UserType.NORMAL)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.user"));
    return userMapper.entityToModel(utilisateurEntity);
  }

  @Override
  public UtilisateurModel findAllTypeOfUserById(Long id) {
    UtilisateurEntity utilisateurEntity =
        userJpaRepo.findById(id).orElseThrow(() -> exceptionFactory.notFound("notfound.user"));
    return userMapper.entityToModel(utilisateurEntity);
  }

  @Override
  public void deleteAll() {
    userJpaRepo.deleteAll(UserType.NORMAL);
  }

  @Override
  public UtilisateurModel findByActivationToken(String token) {
    UtilisateurEntity utilisateurEntity = userJpaRepo.findByActivationToken(token, UserType.NORMAL)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.token"));
    log.info("userEntity in findByActivationToken: {}", utilisateurEntity);
    return userMapper.entityToModel(utilisateurEntity);
  }

  @Override
  public UtilisateurModel findByResetPasswordToken(String token) {
    UtilisateurEntity utilisateurEntity =
        userJpaRepo.findbyResetPasswordToken(token, UserType.NORMAL)
            .orElseThrow(() -> exceptionFactory.notFound("notfound.token"));
    log.info("userEntity in findbyResetPasswordToken: {}", utilisateurEntity);
    return userMapper.entityToModel(utilisateurEntity);
  }

  @Override
  public UtilisateurModel update(UtilisateurModel utilisateurModel) {
    UtilisateurEntity updatedUser = userJpaRepo.save(userMapper.modelToEntity(utilisateurModel));
    return userMapper.entityToModel(updatedUser);
  }

  @Override
  public List<UtilisateurModel> findAll() {
    return userJpaRepo.findAll(UserType.NORMAL).stream().map(userMapper::entityToModel).toList();
  }

  @Override
  public void deleteById(Long id) {
    userJpaRepo.deleteById(id, UserType.NORMAL);
  }

  @Override
  public List<UtilisateurModel> findByRole(Role role) {
    return userJpaRepo.findByRole(role, UserType.NORMAL).stream().map(userMapper::entityToModel)
        .collect(Collectors.toList());
  }



}


