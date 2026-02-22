package tn.temporise.domain.port;

import tn.temporise.domain.model.LigneCommande;

import java.util.List;

public interface LigneCommandeRepo {
  LigneCommande save(LigneCommande ligneCommande);

  LigneCommande findById(Long id);

  List<LigneCommande> findAll();

  LigneCommande findByCommandeIdAndVariantId(Long commandeId, Long variantId);

  void deleteById(Long id);

  void deleteAll();

  List<LigneCommande> findByProductId(Long id);

  List<LigneCommande> findByCommandeId(Long id); // <LigneCommande>
}
