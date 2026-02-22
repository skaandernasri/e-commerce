package tn.temporise.domain.port;

import tn.temporise.domain.model.Paiement;

import java.util.List;

public interface PaiementRepo {
  Paiement save(Paiement paiement);

  Paiement findById(Long id);

  Paiement findByReference(String reference);

  List<Paiement> findByCommandeId(Long id);

  boolean existsByReferenceAndUserId(String reference, Long userId);

}
