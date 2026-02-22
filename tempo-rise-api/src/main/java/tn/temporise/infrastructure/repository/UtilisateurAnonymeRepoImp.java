package tn.temporise.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import tn.temporise.application.mapper.UtilisateurAnonymeMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.UtilisateurAnonyme;
import tn.temporise.domain.port.UtilisateurAnonymeRepo;
import tn.temporise.infrastructure.persistence.entity.UtilisateurAnonymeEntity;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UtilisateurAnonymeRepoImp implements UtilisateurAnonymeRepo {
  private final UtilisateurAnonymeJpaRepo utilisateurAnonymeJpaRepo;
  private final UserJpaRepo userJpaRepo;
  private final ExceptionFactory exceptionFactory;
  private final UtilisateurAnonymeMapper utilisateurAnonymeMapper;

  @Override
  public UtilisateurAnonyme save(UtilisateurAnonyme utilisateurAnonyme) {
    UtilisateurAnonymeEntity utilisateurAnonymeEntity =
        utilisateurAnonymeMapper.modelToEntity(utilisateurAnonyme);
    return utilisateurAnonymeMapper.entityToModel(userJpaRepo.save(utilisateurAnonymeEntity));
  }

  @Override
  public UtilisateurAnonyme findBySessionToken(UUID sessionToken) {
    UtilisateurAnonymeEntity utilisateurAnonymeEntity =
        utilisateurAnonymeJpaRepo.findBySessionToken(sessionToken);
    if (utilisateurAnonymeEntity == null) {
      throw exceptionFactory.notFound("notfound.anonyme_user");
    }
    return utilisateurAnonymeMapper.entityToModel(utilisateurAnonymeEntity);
  }

  @Override
  public UtilisateurAnonyme findById(Long id) {
    return utilisateurAnonymeMapper.entityToModel(utilisateurAnonymeJpaRepo.findById(id)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.anonyme_user")));
  }

  @Override
  public List<UtilisateurAnonyme> findAll() {
    return utilisateurAnonymeJpaRepo.findAll().stream().map(utilisateurAnonymeMapper::entityToModel)
        .toList();
  }
}
