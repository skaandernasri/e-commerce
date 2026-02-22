package tn.temporise.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.temporise.infrastructure.persistence.entity.WhishlistEntity;

import java.util.Optional;

@Repository
public interface WhishlistJpaRepo extends JpaRepository<WhishlistEntity, Long> {
  Optional<WhishlistEntity> findByUtilisateurId(Long userId);
}
