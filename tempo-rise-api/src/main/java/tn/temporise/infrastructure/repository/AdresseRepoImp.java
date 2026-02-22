package tn.temporise.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import tn.temporise.application.mapper.AdresseMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.Adresse;
import tn.temporise.domain.model.TypeAdresse;
import tn.temporise.domain.port.AdresseRepo;
import tn.temporise.infrastructure.persistence.entity.AdresseEntity;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class AdresseRepoImp implements AdresseRepo {
  private final AdresseJpaRepo adresseJpaRepo;
  private final AdresseMapper adresseMapper;
  private final ExceptionFactory exceptionFactory;

  @Override
  public Adresse save(Adresse adresse) {
    AdresseEntity adresseEntity = adresseJpaRepo.save(adresseMapper.modelToEntity(adresse));
    return adresseMapper.entityToModel(adresseEntity);
  }

  @Override
  public Adresse findById(Long id) {
    return adresseJpaRepo.findById(id).map(adresseMapper::entityToModel)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.adresse"));
  }

  @Override
  public List<Adresse> findAll() {
    return adresseJpaRepo.findAll().stream().map(adresseMapper::entityToModel).toList();
  }


  @Override
  public void deleteById(Long id) {
    adresseJpaRepo.deleteById(id);
  }

  @Override
  public void delete(Adresse adresse) {
    adresseJpaRepo.delete(adresseMapper.modelToEntity(adresse));
  }

  @Override
  public void deleteAll() {
    adresseJpaRepo.deleteAll();
  }

  @Override
  public List<Adresse> findByType(TypeAdresse type) {
    return adresseJpaRepo.findByType(type).stream().map(adresseMapper::entityToModel).toList();
  }

  @Override
  public List<Adresse> findByUtilisateurId(Long id) {
    return adresseJpaRepo.findByUtilisateur_Id(id).stream().map(adresseMapper::entityToModel)
        .toList();
  }
}
