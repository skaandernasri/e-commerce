package tn.temporise.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import tn.temporise.application.mapper.PromotionMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.Promotion;
import tn.temporise.domain.port.PromotionRepo;
import tn.temporise.infrastructure.persistence.entity.PromotionEntity;

import java.time.LocalDateTime;
import java.util.List;


@Repository
@RequiredArgsConstructor
@Slf4j
public class PromotionRepoImp implements PromotionRepo {
  private final PromotionJpaRepo promotionJpaRepo;
  private final PromotionMapper promotionMapper;
  private final ExceptionFactory exceptionFactory;

  @Override
  public Promotion save(Promotion promotion) {
    log.info("promotion enityt: {}", promotionMapper.modelToEntity(promotion));
    return promotionMapper
        .entityToModel(promotionJpaRepo.save(promotionMapper.modelToEntity(promotion)));
  }

  @Override
  public Promotion findById(Long id) {
    log.info("Finding promotion by ID: {}", id);
    PromotionEntity promotionEntity = (promotionJpaRepo.findById(id)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.promotion")));
    return promotionMapper.entityToModel(promotionEntity);
  }

  @Override
  public List<Promotion> findAll() {
    log.info("Fetching all promotions");
    List<PromotionEntity> promotionEntity = promotionJpaRepo.findAll();
    return promotionEntity.stream().map(promotionMapper::entityToModel).toList();
  }

  @Override
  public Promotion update(Promotion promotion) {
    return promotionJpaRepo.findById(promotion.id()).map(existingEntity -> {
      PromotionEntity updatedEntity = promotionMapper.modelToEntity(promotion);
      return promotionMapper.entityToModel(promotionJpaRepo.save(updatedEntity));
    }).orElseThrow(() -> exceptionFactory.notFound("notfound.promotion"));
  }

  @Override
  public void deleteById(Long id) {
    promotionJpaRepo.deleteById(id);
  }

  @Override
  public void deleteAll() {
    promotionJpaRepo.deleteAll();
  }

  @Override
  public List<Promotion> findByProduitId(Long id) {
    List<PromotionEntity> promotionEntities = promotionJpaRepo.findByProduitId(id)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.promotion"));
    return promotionEntities.stream().map(promotionMapper::entityToModel).toList();
  }

  @Override
  public List<Promotion> findActivePromotions(LocalDateTime currentDate) {
    List<PromotionEntity> promotionEntities = promotionJpaRepo.findActivePromotions(currentDate);
    return promotionEntities.stream().map(promotionMapper::entityToModel).toList();
  }

  @Override
  public Promotion findActivePromotionByProduct(LocalDateTime currentDate, Long productId) {
    return promotionMapper.entityToModel(
        promotionJpaRepo.findActivePromotionByProduct(currentDate, productId).orElse(null));
  }

  @Override
  public List<Promotion> findInActivePromotions(LocalDateTime currentDate) {
    List<PromotionEntity> promotionEntities = promotionJpaRepo.findInactivePromotions(currentDate);
    return promotionEntities.stream().map(promotionMapper::entityToModel).toList();
  }

  @Override
  public List<Promotion> saveAll(List<Promotion> promotions) {
    List<PromotionEntity> promotionEntities =
        promotionJpaRepo.saveAll(promotions.stream().map(promotionMapper::modelToEntity).toList());
    return promotionEntities.stream().map(promotionMapper::entityToModel).toList();
  }
}
