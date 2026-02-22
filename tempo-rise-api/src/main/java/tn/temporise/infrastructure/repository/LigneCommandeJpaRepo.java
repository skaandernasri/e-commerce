package tn.temporise.infrastructure.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.temporise.infrastructure.persistence.entity.LigneCommandeEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface LigneCommandeJpaRepo extends JpaRepository<LigneCommandeEntity, Long> {
  @Query("SELECT c FROM LigneCommandeEntity c WHERE c.id = :id")
  Optional<LigneCommandeEntity> findById(@Param("id") Long id);

  @Modifying
  @Transactional
  @Query("DELETE FROM LigneCommandeEntity c WHERE c.id = :id")
  void deleteById(@Param("id") Long id);

  @Query("SELECT p from LigneCommandeEntity p WHERE p.variant.id = :id")
  List<LigneCommandeEntity> findProductId(@Param("id") Long id);

  @Query("SELECT p from LigneCommandeEntity p WHERE p.commande.id = :id")
  List<LigneCommandeEntity> findCommandeId(@Param("id") Long id);

  Optional<LigneCommandeEntity> findByCommande_IdAndVariant_Id(Long commandeId, Long variantId);
}
