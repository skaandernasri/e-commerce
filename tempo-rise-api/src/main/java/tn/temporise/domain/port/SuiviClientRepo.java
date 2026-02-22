package tn.temporise.domain.port;

import tn.temporise.domain.model.SuiviClient;
import tn.temporise.domain.model.TypeAction;

import java.util.List;
import java.util.Optional;

public interface SuiviClientRepo {
  SuiviClient save(SuiviClient suiviClient);

  List<SuiviClient> findByUtilisateurAnonymeId(Long id);

  List<SuiviClient> findByUtilisateurId(Long id);

  Optional<SuiviClient> findByUserAndProduitAndType(Long userId, Long produitId,
      TypeAction typeAction);

  Optional<SuiviClient> findByAnonymousAndProduitAndType(Long anonymousId, Long produitId,
      TypeAction typeAction);
}
