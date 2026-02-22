package tn.temporise.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.temporise.infrastructure.persistence.entity.UtilisateurAnonymeEntity;

import java.util.UUID;

@Repository
public interface UtilisateurAnonymeJpaRepo extends JpaRepository<UtilisateurAnonymeEntity, Long> {
  UtilisateurAnonymeEntity findBySessionToken(UUID uuid);
}
