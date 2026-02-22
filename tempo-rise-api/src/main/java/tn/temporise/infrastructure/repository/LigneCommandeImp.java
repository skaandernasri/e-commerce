package tn.temporise.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import tn.temporise.application.mapper.LigneCommandeMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.LigneCommande;
import tn.temporise.domain.port.LigneCommandeRepo;
import tn.temporise.infrastructure.persistence.entity.LigneCommandeEntity;

import java.util.List;

@Repository
@Slf4j
@RequiredArgsConstructor
public class LigneCommandeImp implements LigneCommandeRepo {
  private final LigneCommandeJpaRepo ligneCommandeJpaRepo;
  private final LigneCommandeMapper ligneCommandeMapper;
  private final ExceptionFactory exceptionFactory;

  @Override
  public LigneCommande save(LigneCommande ligneCommande) {
    LigneCommandeEntity ligneCommandeEntity =
        ligneCommandeJpaRepo.save(ligneCommandeMapper.modelToEntity(ligneCommande));
    log.info("ligne commande entity: {}", ligneCommandeEntity);
    return ligneCommandeMapper.entityToModel(ligneCommandeEntity);
  }

  @Override
  public LigneCommande findById(Long id) {
    LigneCommandeEntity ligneCommandeEntity = ligneCommandeJpaRepo.findById(id)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.lignecommande"));
    return ligneCommandeMapper.entityToModel(ligneCommandeEntity);
  }

  @Override
  public List<LigneCommande> findAll() {
    List<LigneCommandeEntity> ligneCommandeEntities = ligneCommandeJpaRepo.findAll();
    if (ligneCommandeEntities.isEmpty())
      throw exceptionFactory.notFound("notfound.no_lignecommandes");
    return ligneCommandeEntities.stream().map(ligneCommandeMapper::entityToModel).toList();
  }

  @Override
  public LigneCommande findByCommandeIdAndVariantId(Long commandeId, Long variantId) {
    LigneCommandeEntity ligneCommandeEntity =
        ligneCommandeJpaRepo.findByCommande_IdAndVariant_Id(commandeId, variantId)
            .orElseThrow(() -> exceptionFactory.notFound("notfound.lignecommande"));
    return ligneCommandeMapper.entityToModel(ligneCommandeEntity);
  }

  @Override
  public void deleteById(Long id) {
    findById(id);
    ligneCommandeJpaRepo.deleteById(id);
  }

  @Override
  public void deleteAll() {
    findAll();
    ligneCommandeJpaRepo.deleteAll();
  }

  @Override
  public List<LigneCommande> findByProductId(Long id) {
    List<LigneCommandeEntity> ligneCommandeEntity = ligneCommandeJpaRepo.findProductId(id);
    if (ligneCommandeEntity.isEmpty())
      throw exceptionFactory.notFound("notfound.no_lignecommandes");
    return ligneCommandeEntity.stream().map(ligneCommandeMapper::entityToModel).toList();
  }

  @Override
  public List<LigneCommande> findByCommandeId(Long id) {
    List<LigneCommandeEntity> ligneCommandeEntities = ligneCommandeJpaRepo.findCommandeId(id);
    if (ligneCommandeEntities.isEmpty())
      throw exceptionFactory.notFound("notfound.no_lignecommandes");
    return ligneCommandeEntities.stream().map(ligneCommandeMapper::entityToModel).toList();
  }
}
