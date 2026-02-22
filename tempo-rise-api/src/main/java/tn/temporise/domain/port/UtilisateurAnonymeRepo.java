package tn.temporise.domain.port;

import tn.temporise.domain.model.UtilisateurAnonyme;

import java.util.List;
import java.util.UUID;

public interface UtilisateurAnonymeRepo {
  UtilisateurAnonyme save(UtilisateurAnonyme utilisateurAnonyme);

  UtilisateurAnonyme findBySessionToken(UUID sessionToken);

  UtilisateurAnonyme findById(Long id);

  List<UtilisateurAnonyme> findAll();
}
