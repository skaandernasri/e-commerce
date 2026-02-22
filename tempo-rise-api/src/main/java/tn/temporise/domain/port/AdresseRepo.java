package tn.temporise.domain.port;

import tn.temporise.domain.model.Adresse;
import tn.temporise.domain.model.TypeAdresse;

import java.util.List;

public interface AdresseRepo {
  Adresse save(Adresse adresse);

  Adresse findById(Long id);

  List<Adresse> findAll();

  void deleteById(Long id);

  void delete(Adresse adresse);

  void deleteAll();

  List<Adresse> findByType(TypeAdresse type);

  List<Adresse> findByUtilisateurId(Long id);
}


