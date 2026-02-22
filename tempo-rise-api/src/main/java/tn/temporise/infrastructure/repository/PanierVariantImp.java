package tn.temporise.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import tn.temporise.application.mapper.PanierProduitMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.PanierVariant;
import tn.temporise.domain.port.PanierVariantRepo;
import tn.temporise.infrastructure.persistence.entity.PanierVariantEntity;
import tn.temporise.infrastructure.persistence.entity.PanierVariantId;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Slf4j
@RequiredArgsConstructor
public class PanierVariantImp implements PanierVariantRepo {
  private final PanierVariantJpaRepo panierVariantJpaRepo;
  private final PanierProduitMapper panierProduitMapper;
  private final ExceptionFactory exceptionFactory;

  @Override
  public PanierVariant save(PanierVariant panierVariant) {
    PanierVariantEntity entity = panierProduitMapper.modelToEntity(panierVariant);
    PanierVariantEntity saved = panierVariantJpaRepo.save(entity);
    return panierProduitMapper.entityToModel(saved);
  }

  @Override
  public PanierVariant findById(PanierVariantId id) {
    return panierVariantJpaRepo.findById(id).map(panierProduitMapper::entityToModel)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.panierproduit"));
  }

  @Override
  public List<PanierVariant> findAll() {
    if (panierVariantJpaRepo.findAll().isEmpty())
      throw exceptionFactory.notFound("notfound.no_panierproduits");
    return panierVariantJpaRepo.findAll().stream().map(panierProduitMapper::entityToModel)
        .collect(Collectors.toList());
  }

  @Override
  public PanierVariant update(PanierVariant panierVariant) {
    return panierVariantJpaRepo
        .findById(new PanierVariantId(panierVariant.panier().id(), panierVariant.variant().id()))
        .map(existingEntity -> {
          PanierVariantEntity updatedEntity = panierProduitMapper.modelToEntity(panierVariant);
          return panierProduitMapper.entityToModel(panierVariantJpaRepo.save(updatedEntity));
        }).orElseThrow(() -> exceptionFactory.notFound("notfound.panierproduit"));

  }

  @Override
  public void deleteById(PanierVariantId id) {
    panierVariantJpaRepo.deleteById(id);
  }

  @Override
  public void deleteByPanierId(Long id) {
    panierVariantJpaRepo.deleteByPanierId(id);
  }

  @Override
  public void deleteAll() {
    panierVariantJpaRepo.deleteAll();
  }

  @Override
  public List<PanierVariant> findByPanierId(Long id) {
    List<PanierVariantEntity> panierVariantEntities = panierVariantJpaRepo.findByPanierId(id)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.no_panierproduits"));
    List<PanierVariant> panierVariants =
        panierVariantEntities.stream().map(panierProduitMapper::entityToModel).toList();
    log.info("panierProduits: {}", panierVariants);
    return panierVariantEntities.stream().map(panierProduitMapper::entityToModel).toList();
  }

  @Override
  public PanierVariant findByPanierIdAndVariantId(Long panierId, Long variantId) {
    PanierVariantEntity panierVariantEntity =
        panierVariantJpaRepo.findByPanierIdAndVariantId(panierId, variantId)
            .orElseThrow(() -> exceptionFactory.notFound("notfound.panierproduit"));
    return panierProduitMapper.entityToModel(panierVariantEntity);
  }

  @Override
  public void clearExpiredReservations(LocalDateTime now) {
    panierVariantJpaRepo.deleteByExpirationDateBefore(now);
  }

  @Override
  public List<PanierVariant> findByVariantId(Long id) {
    List<PanierVariantEntity> panierProduitEntities = panierVariantJpaRepo.findByVariant_Id(id);
    log.debug("panierProduitsEntities: {}", panierProduitEntities);
    return panierProduitEntities.stream().map(panierProduitMapper::entityToModel).toList();
  }

  @Override
  public List<PanierVariant> findByVariantIdAndUserIdDifferent(Long id, Long userId) {
    List<PanierVariantEntity> panierProduitEntities =
        panierVariantJpaRepo.findByVariant_IdAndUserIdDiffernt(id, userId);
    log.debug("panierProduitsEntities: {}", panierProduitEntities);
    return panierProduitEntities.stream().map(panierProduitMapper::entityToModel).toList();
  }
}
