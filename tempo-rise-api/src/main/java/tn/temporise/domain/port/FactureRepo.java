package tn.temporise.domain.port;

import tn.temporise.domain.model.Facture;

import java.util.List;

public interface FactureRepo {
  List<Facture> findAll();

  Facture save(Facture facture);

  Facture findById(Long id);

  List<Facture> findByUtilisateurId(Long id);

  Facture findByCommandeId(Long id);

  void deleteById(Long id);

  void deleteAll();
}
