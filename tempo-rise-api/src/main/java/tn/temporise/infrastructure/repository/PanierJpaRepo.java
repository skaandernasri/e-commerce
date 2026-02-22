package tn.temporise.infrastructure.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.temporise.infrastructure.persistence.entity.PanierEntity;

import java.util.Optional;

@Repository
public interface PanierJpaRepo extends JpaRepository<PanierEntity, Long> {
  @Query("SELECT c FROM PanierEntity c WHERE c.id = :id")
  Optional<PanierEntity> findById(@Param("id") Long id);

  @Modifying
  @Transactional
  @Query("DELETE FROM PanierEntity c WHERE c.id = :id")
  void deleteById(@Param("id") Long id);

  @Query("SELECT p from PanierEntity p WHERE p.utilisateur.id = :id")
  Optional<PanierEntity> findByUtilisateurId(@Param("id") Long id);
}
