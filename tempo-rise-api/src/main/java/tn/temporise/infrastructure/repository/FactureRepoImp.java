package tn.temporise.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import tn.temporise.application.mapper.FactureMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.Facture;
import tn.temporise.domain.port.FactureRepo;
import tn.temporise.infrastructure.persistence.entity.FactureEntity;

import java.util.List;

@RequiredArgsConstructor
@Repository
@Slf4j
public class FactureRepoImp implements FactureRepo {
  private final CommandeJpaRepo commandeJpaRepo;
  private final FactureJpaRepo factureJpaRepo;
  private final FactureMapper factureMapper;
  private final ExceptionFactory exceptionFactory;

  @Override
  public List<Facture> findAll() {
    return factureJpaRepo.findAll().stream().map(factureMapper::entityToModel).toList();
  }

  @Override
  public Facture save(Facture facture) {
    FactureEntity factureEntity = factureJpaRepo.save(factureMapper.modelToEntity(facture));
    return factureMapper.entityToModel(factureEntity);
  }

  @Override
  public Facture findById(Long id) {
    FactureEntity factureEntity = factureJpaRepo.findById(id)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.facture"));
    return factureMapper.entityToModel(factureEntity);
  }

  @Override
  public List<Facture> findByUtilisateurId(Long id) {
    return factureJpaRepo.findByUserId(id).stream().map(factureMapper::entityToModel).toList();
  }

  @Override
  public Facture findByCommandeId(Long id) {
    if (factureJpaRepo.findByCommande_Id(id) == null)
      throw exceptionFactory.notFound("notfound.no_factures");
    return factureMapper.entityToModel(factureJpaRepo.findByCommande_Id(id));
  }

  @Override
  public void deleteById(Long id) {
    factureJpaRepo.deleteById(id);
  }

  @Override
  public void deleteAll() {
    // First break all relationships
    factureJpaRepo.findAll().forEach(facture -> {
      if (facture.getCommande() != null) {
        facture.getCommande().setFacture(null); // Break bidirectional link
        commandeJpaRepo.save(facture.getCommande()); // Update the commande
      }
    });

    // Then delete all invoices
    factureJpaRepo.deleteAll();
  }
}
