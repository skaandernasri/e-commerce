package tn.temporise.domain.port;

import tn.temporise.domain.model.PanierVariant;
import tn.temporise.infrastructure.persistence.entity.PanierVariantId;

import java.time.LocalDateTime;
import java.util.List;

public interface PanierVariantRepo {
  PanierVariant save(PanierVariant panierVariant);

  PanierVariant findById(PanierVariantId id);

  List<PanierVariant> findAll();

  PanierVariant update(PanierVariant panier);

  void deleteById(PanierVariantId id);

  void deleteByPanierId(Long id);

  void deleteAll();

  List<PanierVariant> findByPanierId(Long id);

  PanierVariant findByPanierIdAndVariantId(Long panierId, Long produitId);

  void clearExpiredReservations(LocalDateTime date);

  List<PanierVariant> findByVariantId(Long id);

  List<PanierVariant> findByVariantIdAndUserIdDifferent(Long id, Long userId);
}
