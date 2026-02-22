package tn.temporise.domain.port;

import tn.temporise.domain.model.Categorie;

import java.util.List;

public interface CategorieRepo {
  Categorie save(Categorie categorie);

  Categorie findById(Long id);

  List<Categorie> findAll();

  Categorie update(Categorie categorie);

  void deleteById(Long id);

  void deleteAll();
}
