package tn.temporise.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.temporise.infrastructure.persistence.entity.PaiementEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaiementJpaRepo extends JpaRepository<PaiementEntity, Long> {
  List<PaiementEntity> findByCommande_Id(Long id);

  boolean existsByPaiementRefAndCommande_User_Id(String reference, Long userId);

  Optional<PaiementEntity> findByPaiementRef(String reference);
}
