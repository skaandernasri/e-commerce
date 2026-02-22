package tn.temporise.domain.port;

import tn.temporise.domain.model.Panier;

import java.util.List;

public interface PanierRepo {
  Panier save(Panier panier);

  Panier findById(Long id);

  List<Panier> findAll();

  Panier update(Panier panier);

  void deleteById(Long id);

  void deleteAll();

  Panier findByUtilisateurId(Long id);
}
