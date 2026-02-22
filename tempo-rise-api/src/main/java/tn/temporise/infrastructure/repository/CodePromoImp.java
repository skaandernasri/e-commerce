package tn.temporise.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import tn.temporise.application.mapper.CodePromoMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.CodePromo;
import tn.temporise.domain.port.CodePromoRepo;
import tn.temporise.infrastructure.persistence.entity.CodePromoEntity;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@Slf4j
@RequiredArgsConstructor
public class CodePromoImp implements CodePromoRepo {
  private final CodePromoJpaRepo codePromoJpaRepo;
  private final CodePromoMapper codePromoMapper;
  private final ExceptionFactory exceptionFactory;

  @Override
  public CodePromo save(CodePromo codePromo) {
    CodePromoEntity codePromoEntity = codePromoMapper.modelToEntity(codePromo);
    return codePromoMapper.entityToModel(codePromoJpaRepo.save(codePromoEntity));
  }

  @Override
  public CodePromo findById(Long id) {
    CodePromoEntity codePromoEntity = codePromoJpaRepo.findById(id)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.codePromo"));
    return codePromoMapper.entityToModel(codePromoEntity);
  }

  @Override
  public List<CodePromo> findAll() {
    List<CodePromoEntity> codePromos = codePromoJpaRepo.findAll();
    return codePromos.stream().map(codePromoMapper::entityToModel).toList();
  }

  @Override
  public void deleteById(Long id) {
    findById(id);
    codePromoJpaRepo.deleteById(id);
  }

  @Override
  public void deleteAll() {
    codePromoJpaRepo.deleteAll();
  }

  @Override
  public CodePromo findByCode(String code) {
    CodePromoEntity codePromoEntity = codePromoJpaRepo.findByCode(code)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.codePromo"));
    return codePromoMapper.entityToModel(codePromoEntity);
  }

  @Override
  public boolean existsByCode(String code) {
    return codePromoJpaRepo.existsByCode(code);
  }

  @Override
  public List<CodePromo> findByExpirationDateLessThanEqual(LocalDateTime currentDate) {
    List<CodePromoEntity> codePromoEntities =
        codePromoJpaRepo.findBydateExpirationLessThanEqual(currentDate);

    return codePromoEntities.stream().map(codePromoMapper::entityToModel).toList();
  }

  @Override
  public List<CodePromo> findByExpirationDateGreaterThan(LocalDateTime currentDate) {
    List<CodePromoEntity> codePromoEntities =
        codePromoJpaRepo.findBydateExpirationGreaterThan(currentDate);

    return codePromoEntities.stream().map(codePromoMapper::entityToModel).toList();
  }
}
