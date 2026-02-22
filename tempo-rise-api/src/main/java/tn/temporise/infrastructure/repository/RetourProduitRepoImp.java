package tn.temporise.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import tn.temporise.application.mapper.RetourProduitMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.RetourProduit;
import tn.temporise.domain.port.RetourProduitRepo;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RetourProduitRepoImp implements RetourProduitRepo {
  private final RetourProduitJpaRepo retourProduitJpaRepo;
  private final ExceptionFactory exceptionFactory;
  private final RetourProduitMapper retourProduitMapper;

  @Override
  public RetourProduit save(RetourProduit retourProduit) {
    return retourProduitMapper.retourProduitEntityToRetourProduit(retourProduitJpaRepo
        .save(retourProduitMapper.retourProduitToRetourProduitEntity(retourProduit)));
  }

  @Override
  public List<RetourProduit> findAll() {
    return retourProduitJpaRepo.findAll().stream()
        .map(retourProduitMapper::retourProduitEntityToRetourProduit).toList();
  }

  @Override
  public RetourProduit findById(Long id) {
    return retourProduitJpaRepo.findById(id)
        .map(retourProduitMapper::retourProduitEntityToRetourProduit)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.retour_produit"));
  }
}
