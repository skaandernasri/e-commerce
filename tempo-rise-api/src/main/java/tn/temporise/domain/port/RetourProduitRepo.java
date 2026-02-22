package tn.temporise.domain.port;

import tn.temporise.domain.model.RetourProduit;

import java.util.List;

public interface RetourProduitRepo {
  RetourProduit save(RetourProduit retourProduit);

  List<RetourProduit> findAll();

  RetourProduit findById(Long id);
}
