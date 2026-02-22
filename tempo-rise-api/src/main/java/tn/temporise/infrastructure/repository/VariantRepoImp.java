package tn.temporise.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import tn.temporise.application.mapper.VariantMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.Variant;
import tn.temporise.domain.port.VariantRepo;
import tn.temporise.infrastructure.persistence.entity.VariantEntity;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class VariantRepoImp implements VariantRepo {
  private final VariantJpaRepo variantJpaRepo;
  private final VariantMapper variantMapper;
  private final ExceptionFactory exceptionFactory;

  @Override
  public Variant save(Variant variant) {
    VariantEntity variantEntity = variantJpaRepo.save(variantMapper.modelToEntity(variant));
    log.info("Variant entity saved: " + variantEntity);
    log.info("Variant model saved: " + variantMapper.entityToModel(variantEntity));
    return variantMapper.entityToModel(variantEntity);
  }

  @Override
  public Variant findById(Long id) {
    log.info("variant log model after mapping: {}",
        variantJpaRepo.findById(id).map(variantMapper::entityToModel));
    return variantJpaRepo.findById(id).map(variantMapper::entityToModel)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.variant"));
  }

  @Override
  public List<Variant> findAll() {
    return variantJpaRepo.findAll().stream().map(variantMapper::entityToModel)
        .collect(Collectors.toList());
  }

  @Override
  public Variant findByIdForUpdate(Long id) {
    return variantJpaRepo.findByIdWithLock(id).map(variantMapper::entityToModel)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.variant"));
  }

  @Override
  public boolean existsByColorAndSizeAndProduitId(String color, String size, Long productId) {
    return variantJpaRepo.existsByColorAndSizeAndProduit_Id(color, size, productId);
  }

  @Override
  public void saveAll(List<Variant> variants) {
    log.info("Saving {} variants", variants);
    List<VariantEntity> variantEntities =
        variants.stream().map(variantMapper::modelToEntity).toList();
    log.info("Saving {} variants entites", variantEntities);

    variantJpaRepo.saveAll(variantEntities);
  }

  @Override
  public void deleteById(Long id) {
    variantJpaRepo.deleteById(id);
  }

  @Override
  public Long countByQuantityreaterThan(Long quantity) {
    log.info("Counting variants with quantity greater than {}", quantity);
    return variantJpaRepo.countByQuantityGreaterThan(quantity);
  }

  @Override
  public Long countByQuantityLessThan(Long quantity) {
    log.info("Counting variants with quantity less than {}", quantity);
    return variantJpaRepo.countByQuantityLessThanEqual(quantity);
  }

  @Override
  public Long countByQuantityEquals(Long quantity) {
    log.info("Counting variants with quantity equals {}", quantity);
    return variantJpaRepo.countByQuantityEquals(quantity);
  }
}
