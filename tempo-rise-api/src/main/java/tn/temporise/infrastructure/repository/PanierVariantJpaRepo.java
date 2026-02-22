package tn.temporise.infrastructure.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.temporise.infrastructure.persistence.entity.PanierVariantEntity;
import tn.temporise.infrastructure.persistence.entity.PanierVariantId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PanierVariantJpaRepo extends JpaRepository<PanierVariantEntity, PanierVariantId> {
  @Query("SELECT c FROM PanierVariantEntity c WHERE c.id = :id")
  Optional<PanierVariantEntity> findById(@Param("id") PanierVariantId id);

  @Modifying
  @Transactional
  @Query("DELETE FROM PanierVariantEntity c WHERE c.id = :id")
  void deleteById(@Param("id") PanierVariantId id);

  @Query("SELECT p from PanierVariantEntity p WHERE p.panier.id = :id")
  Optional<List<PanierVariantEntity>> findByPanierId(@Param("id") Long id);

  @Query("SELECT p from PanierVariantEntity p WHERE p.panier.id = :id and p.variant.id = :idVariant")
  Optional<PanierVariantEntity> findByPanierIdAndVariantId(@Param("id") Long id,
      @Param("idVariant") Long idVariant);

  @Modifying
  @Transactional
  @Query("DELETE FROM PanierVariantEntity p WHERE p.panier.id = :id")
  void deleteByPanierId(@Param("id") Long id);

  @Modifying
  @Transactional
  void deleteByExpirationDateBefore(LocalDateTime now);

  List<PanierVariantEntity> findByVariant_Id(Long id);

  @Query("SELECT p from PanierVariantEntity p WHERE p.variant.id = :id and p.panier.utilisateur.id != :userId")
  List<PanierVariantEntity> findByVariant_IdAndUserIdDiffernt(@Param("id") Long id,
      @Param("userId") Long userId);
}
