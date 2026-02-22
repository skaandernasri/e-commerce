package tn.temporise.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import tn.temporise.application.mapper.CategorieMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.Categorie;
import tn.temporise.domain.port.CategorieRepo;
import tn.temporise.infrastructure.persistence.entity.CategorieEntity;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CategorieRepoImp implements CategorieRepo {
  private final CategorieJpaRepo categorieJpaRepo;
  private final CategorieMapper categorieMapper;
  private final ExceptionFactory exceptionFactory;

  @Override
  public Categorie save(Categorie categorie) {
    CategorieEntity categorieEntity =
        categorieJpaRepo.save(categorieMapper.modelToEntity(categorie));
    return categorieMapper.entityToModel(categorieEntity);
  }

  @Override
  public Categorie findById(Long id) {
    return categorieJpaRepo.findById(id).map(categorieMapper::entityToModel)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.category"));
  }

  @Override
  public List<Categorie> findAll() {
    return categorieJpaRepo.findAll().stream().map(categorieMapper::entityToModel)
        .collect(Collectors.toList());
  }

  @Override
  public Categorie update(Categorie categorie) {
    return categorieJpaRepo.findById(categorie.id()).map(existingEntity -> {
      CategorieEntity updatedEntity = categorieMapper.modelToEntity(categorie);
      updatedEntity.setId(existingEntity.getId()); // Assurer la conservation de l'ID
      return categorieMapper.entityToModel(categorieJpaRepo.save(updatedEntity));
    }).orElseThrow(() -> exceptionFactory.notFound("notfound.category"));
  }

  @Override
  public void deleteById(Long id) {
    categorieJpaRepo.deleteById(id);
  }

  @Override
  public void deleteAll() {
    categorieJpaRepo.deleteAll();
  }
}
