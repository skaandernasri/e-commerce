package tn.temporise.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import tn.temporise.application.mapper.SectionMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.Section;
import tn.temporise.domain.model.TypePage;
import tn.temporise.domain.model.TypeSection;
import tn.temporise.domain.port.SectionRepo;
import tn.temporise.infrastructure.persistence.entity.SectionEntity;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@Slf4j
@RequiredArgsConstructor
public class SectionRepoImp implements SectionRepo {
  private final SectionJpaRepo sectionJpaRepo;
  private final SectionMapper sectionMapper;
  private final ExceptionFactory exceptionFactory;


  @Override
  public Section save(Section section) {
    return sectionMapper.entityToModel(sectionJpaRepo.save(sectionMapper.modelToEntity(section)));
  }

  @Override
  public void deleteAll() {
    sectionJpaRepo.deleteAll();
  }

  @Override
  public List<Section> saveAll(List<Section> sections) {
    List<SectionEntity> sectionEntities =
        sections.stream().map(sectionMapper::modelToEntity).toList();
    List<SectionEntity> savedSectionEntities = sectionJpaRepo.saveAll(sectionEntities);
    return savedSectionEntities.stream().map(sectionMapper::entityToModel).toList();
  }

  @Override
  public Section findById(Long id) {
    SectionEntity sectionEntity = sectionJpaRepo.findById(id)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.section"));
    return sectionMapper.entityToModel(sectionEntity);
  }

  @Override
  public void checkAllTypeInactive(TypeSection typeSection, TypePage typePage) {
    sectionJpaRepo.checkAllTypeInactive(typeSection, typePage);
  }

  @Override
  public List<Section> findAll() {
    List<SectionEntity> sectionEntities = sectionJpaRepo.findAll();
    return sectionEntities.stream().map(sectionMapper::entityToModel).collect(Collectors.toList());
  }

  @Override
  public void delete(Long id) {
    sectionJpaRepo.deleteById(id);
  }

  @Override
  public Section findByTitre(String titre) {
    return sectionMapper.entityToModel(sectionJpaRepo.findByTitre(titre)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.section")));
  }

  @Override
  public List<Section> findByType(TypeSection type) {
    return sectionJpaRepo.findByType(type).stream().map(sectionMapper::entityToModel)
        .collect(Collectors.toList());
  }

  @Override
  public List<Section> findByTypePage(TypePage typePage) {
    return sectionJpaRepo.findByTypePage(typePage).stream().map(sectionMapper::entityToModel)
        .collect(Collectors.toList());
  }
}
