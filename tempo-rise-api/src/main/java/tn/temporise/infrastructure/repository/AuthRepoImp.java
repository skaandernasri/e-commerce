package tn.temporise.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import tn.temporise.application.mapper.AuthMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.Authentification;
import tn.temporise.domain.port.AuthRepo;
import tn.temporise.infrastructure.persistence.entity.AuthentificationEntity;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class AuthRepoImp implements AuthRepo {
  private final AuthJpaRepo authJpaRepo;
  private final AuthMapper authMapper;
  private final ExceptionFactory exceptionFactory;

  @Override
  public List<Authentification> findByUserEmail(String email) {
    return authJpaRepo.findByUser_Email(email)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.authentication")).stream()
        .map(authMapper::entityToModel).toList();
  }

  @Override
  public Authentification findByUserEmailAndProviderId(String email, String providerId) {
    AuthentificationEntity authentication =
        authJpaRepo.findByUser_EmailAndProviderId(email, providerId)
            .orElseThrow(() -> exceptionFactory.notFound("notfound.authentication"));
    return authMapper.entityToModel(authentication);
  }

  @Override
  public Authentification findByRefreshToken(String token) {
    AuthentificationEntity authentication = authJpaRepo.findByRefreshToken(token)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.authentication"));
    return authMapper.entityToModel(authentication);
  }

  @Override
  public Authentification save(Authentification authentification) {
    AuthentificationEntity auth = authJpaRepo.save(authMapper.modelToEntity(authentification));
    return authMapper.entityToModel(auth);
  }

  @Override
  public void deleteAll() {
    authJpaRepo.deleteAll();
  }

  @Override
  public void deleteById(Long id) {
    authJpaRepo.findById(id)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.authentication"));
    authJpaRepo.deleteById(id);
  }
}
