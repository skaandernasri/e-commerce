package tn.temporise.infrastructure.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.temporise.infrastructure.persistence.entity.AvisEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AvisJpaRepo
    extends JpaRepository<AvisEntity, Long>, JpaSpecificationExecutor<AvisEntity> {
  @Query("SELECT a FROM AvisEntity a WHERE a.id = :id")
  Optional<AvisEntity> findById(@Param("id") Long id);

  @Modifying
  @Transactional
  @Query("DELETE FROM AvisEntity a WHERE a.id = :id")
  void deleteById(@Param("id") Long id);

  @Query("SELECT a FROM AvisEntity a WHERE a.utilisateur.id = :id")
  Optional<List<AvisEntity>> findByUtilisateurId(@Param("id") Long id);

  @Query("SELECT a FROM AvisEntity a WHERE a.produit.id = :id")
  Optional<List<AvisEntity>> findByProduitId(@Param("id") Long id);

  Page<AvisEntity> findAllByProduit_Id(Long id, Pageable pageable);

  @Query("SELECT AVG(a.note) FROM AvisEntity a")
  Float findAverageRating();

  @Query("SELECT AVG(a.note) FROM AvisEntity a WHERE a.produit.nom = :nom")
  Float findAverageRatingByProduitId(String nom);

  @Query("SELECT COUNT(r) FROM AvisEntity r WHERE r.datePublication BETWEEN :startDate AND :endDate")
  Long findReviewsInPeriod(@Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query("SELECT COUNT(r) FROM AvisEntity r WHERE r.produit.nom = :productName AND r.datePublication BETWEEN :startDate AND :endDate")
  Long findReviewsInPeriodByProduitName(@Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate, @Param("productName") String productName);

  @Query("SELECT AVG(a.note) FROM AvisEntity a WHERE a.datePublication BETWEEN :startDate AND :endDate")
  Float findAverageRatingInPeriod(@Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query("SELECT AVG(a.note) FROM AvisEntity a WHERE a.produit.nom = :productName AND a.datePublication BETWEEN :startDate AND :endDate")
  Float findAverageRatingInPeriodByProduitName(@Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate, @Param("productName") String productName);


}
