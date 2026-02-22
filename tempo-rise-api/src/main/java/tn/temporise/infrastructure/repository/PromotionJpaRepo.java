package tn.temporise.infrastructure.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.temporise.infrastructure.persistence.entity.PromotionEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionJpaRepo extends JpaRepository<PromotionEntity, Long> {
  @Query("SELECT p FROM PromotionEntity p WHERE p.id = :id")
  Optional<PromotionEntity> findById(@Param("id") Long id);

  @Modifying
  @Transactional
  @Query("DELETE FROM PromotionEntity p WHERE p.id = :id")
  void deleteById(@Param("id") Long id);

  @Query("SELECT p FROM PromotionEntity p WHERE p.produit.id = :id")
  Optional<List<PromotionEntity>> findByProduitId(@Param("id") Long id);

  // active
  @Query("SELECT p FROM PromotionEntity p WHERE p.dateDebut <= :currentDate AND p.dateFin > :currentDate")
  List<PromotionEntity> findActivePromotions(@Param("currentDate") LocalDateTime currentDate);

  @Query("SELECT p FROM PromotionEntity p WHERE p.dateDebut <= :currentDate AND p.dateFin > :currentDate AND p.produit.id = :productId")
  Optional<PromotionEntity> findActivePromotionByProduct(LocalDateTime currentDate, Long productId);

  // inactive
  @Query("SELECT p FROM PromotionEntity p WHERE p.dateFin <= :currentDate OR p.dateDebut > :currentDate")
  List<PromotionEntity> findInactivePromotions(@Param("currentDate") LocalDateTime currentDate);
}
