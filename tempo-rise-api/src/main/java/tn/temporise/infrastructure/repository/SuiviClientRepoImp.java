package tn.temporise.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import tn.temporise.application.mapper.SuiviClientMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.SuiviClient;
import tn.temporise.domain.model.TypeAction;
import tn.temporise.domain.port.SuiviClientRepo;
import tn.temporise.infrastructure.persistence.entity.SuiviClientEntity;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class SuiviClientRepoImp implements SuiviClientRepo {
  private final SuiviClientJpaRepo suiviClientJpaRepo;
  private final SuiviClientMapper suiviClientMapper;
  private final ExceptionFactory exceptionFactory;

  @Override
  public SuiviClient save(SuiviClient suiviClient) {
    SuiviClientEntity suiviClientEntity =
        suiviClientJpaRepo.save(suiviClientMapper.modelToEntity(suiviClient));
    log.info("SuiviClient saved: " + suiviClientEntity);
    return suiviClientMapper.entityToModel(suiviClientEntity);
  }

  @Override
  public List<SuiviClient> findByUtilisateurAnonymeId(Long id) {
    return suiviClientJpaRepo.findByUtilisateurAnonyme_Id(id).stream()
        .map(suiviClientMapper::entityToModel).toList();
  }

  @Override
  public List<SuiviClient> findByUtilisateurId(Long id) {
    return suiviClientJpaRepo.findByUtilisateur_Id(id).stream()
        .map(suiviClientMapper::entityToModel).toList();
  }

  @Override
  public Optional<SuiviClient> findByUserAndProduitAndType(Long userId, Long produitId,
      TypeAction typeAction) {
    return suiviClientJpaRepo
        .findByUtilisateur_IdAndProduit_IdAndTypeAction(userId, produitId, typeAction)
        .map(suiviClientMapper::entityToModel);
  }

  @Override
  public Optional<SuiviClient> findByAnonymousAndProduitAndType(Long anonymousId, Long produitId,
      TypeAction typeAction) {
    return suiviClientJpaRepo
        .findByUtilisateurAnonyme_IdAndProduit_IdAndTypeAction(anonymousId, produitId, typeAction)
        .map(suiviClientMapper::entityToModel);
  }
}
