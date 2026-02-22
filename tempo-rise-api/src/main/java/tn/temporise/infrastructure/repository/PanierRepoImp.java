package tn.temporise.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import tn.temporise.application.mapper.PanierMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.Panier;
import tn.temporise.domain.port.PanierRepo;
import tn.temporise.infrastructure.persistence.entity.PanierEntity;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PanierRepoImp implements PanierRepo {
  private final PanierJpaRepo panierJpaRepo;
  private final PanierMapper panierMapper;
  private final ExceptionFactory exceptionFactory;

  @Override
  public Panier save(Panier panier) {
    PanierEntity panierEntity = panierMapper.modelToEntity(panier);
    PanierEntity savedPanier = panierJpaRepo.save(panierEntity);
    return panierMapper.entityToModel(savedPanier);
  }

  @Override
  public Panier findById(Long id) {
    log.info("panier entity{}", panierJpaRepo.findById(id));
    return panierJpaRepo.findById(id).map(panierMapper::entityToModel)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.panier")); // Retourne null si le
                                                                          // produit n'existe pas

  }

  @Override
  public List<Panier> findAll() {
    if (panierJpaRepo.findAll().isEmpty())
      throw exceptionFactory.notFound("notfound.no_paniers");
    return panierJpaRepo.findAll().stream().map(panierMapper::entityToModel)
        .collect(Collectors.toList());
  }

  @Override
  public Panier update(Panier panier) {
    return panierJpaRepo.findById(panier.id()).map(existingEntity -> {
      PanierEntity updatedEntity = panierMapper.modelToEntity(panier);
      return panierMapper.entityToModel(panierJpaRepo.save(updatedEntity));
    }).orElseThrow(() -> exceptionFactory.notFound("notfound.panier"));

  }

  @Override
  public void deleteById(Long id) {
    panierJpaRepo.deleteById(id);
  }

  @Override
  public void deleteAll() {
    panierJpaRepo.deleteAll();
  }

  @Override
  public Panier findByUtilisateurId(Long id) {
    PanierEntity panierEntity = panierJpaRepo.findByUtilisateurId(id)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.panier"));
    return panierMapper.entityToModel(panierEntity);
  }
}
