package tn.temporise.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.temporise.domain.model.TypeAction;
import tn.temporise.infrastructure.persistence.entity.SuiviClientEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface SuiviClientJpaRepo extends JpaRepository<SuiviClientEntity, Long> {
  List<SuiviClientEntity> findByUtilisateurAnonyme_Id(Long id);

  List<SuiviClientEntity> findByUtilisateur_Id(Long id);

  Optional<SuiviClientEntity> findByUtilisateur_IdAndProduit_IdAndTypeAction(Long utilisateurId,
      Long produitId, TypeAction typeAction);

  Optional<SuiviClientEntity> findByUtilisateurAnonyme_IdAndProduit_IdAndTypeAction(
      Long utilisateurAnonymeId, Long produitId, TypeAction typeAction);
}
