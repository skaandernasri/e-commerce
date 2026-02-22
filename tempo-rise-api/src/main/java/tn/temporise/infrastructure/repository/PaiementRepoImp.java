package tn.temporise.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import tn.temporise.application.mapper.PaiementMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.Paiement;
import tn.temporise.domain.port.PaiementRepo;
import tn.temporise.infrastructure.persistence.entity.PaiementEntity;

import java.util.List;

@Repository
@Slf4j
@RequiredArgsConstructor
public class PaiementRepoImp implements PaiementRepo {
  private final ExceptionFactory exceptionFactory;
  private final PaiementMapper paiementMapper;
  private final PaiementJpaRepo paiementJpaRepo;


  @Override
  public Paiement save(Paiement paiement) {
    PaiementEntity paiementEntity = paiementJpaRepo.save(paiementMapper.modelToEntity(paiement));
    return paiementMapper.entityToModel(paiementEntity);
  }

  @Override
  public Paiement findById(Long id) {
    return paiementJpaRepo.findById(id).map(paiementMapper::entityToModel)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.paiement"));
  }

  @Override
  public Paiement findByReference(String reference) {
    return paiementJpaRepo.findByPaiementRef(reference).map(paiementMapper::entityToModel)
        .orElse(Paiement.builder().build());
  }

  @Override
  public List<Paiement> findByCommandeId(Long id) {
    return paiementJpaRepo.findByCommande_Id(id).stream().map(paiementMapper::entityToModel)
        .toList();
  }

  @Override
  public boolean existsByReferenceAndUserId(String reference, Long userId) {
    return paiementJpaRepo.existsByPaiementRefAndCommande_User_Id(reference, userId);
  }
}
