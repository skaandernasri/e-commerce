package tn.temporise.infrastructure.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.temporise.domain.model.StatutCommande;
import tn.temporise.domain.model.UserType;
import tn.temporise.infrastructure.persistence.entity.CommandeEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommandeJpaRepo
    extends JpaRepository<CommandeEntity, Long>, JpaSpecificationExecutor<CommandeEntity> {
  @Query("SELECT c from CommandeEntity c WHERE c.user.id = :id")
  Optional<List<CommandeEntity>> findByUserId(@Param("id") Long id);

  @Query("SELECT c from CommandeEntity c WHERE c.codePromo.id = :id")
  Optional<CommandeEntity> findByCodePromoId(Long id);

  // @Query("SELECT c FROM CommandeEntity c WHERE c.id = :id")
  // Optional<CommandeEntity> findById(@Param("id") Long id);
  @Modifying
  @Transactional
  @Query("DELETE FROM CommandeEntity c WHERE c.id = :id")
  void deleteById(@Param("id") Long id);

  boolean existsByIdAndUser_Id(Long id, Long userId);

  boolean existsByIdAndUser_IdAndUser_UserType(Long id, Long userId, UserType userType);

  List<CommandeEntity> findByAdresseLivraison_Id(Long id);

  List<CommandeEntity> findByAdresseFacturation_Id(Long id);

  // In CommandeRepo interface
  @Query("SELECT c FROM CommandeEntity c LEFT JOIN FETCH c.lignesCommande WHERE c.id = :id")
  Optional<CommandeEntity> findByIdWithLignes(@Param("id") Long id);

  @Query("SELECT c FROM CommandeEntity c LEFT JOIN FETCH c.lignesCommande WHERE c.user.id = :userId")
  Optional<List<CommandeEntity>> findByUserIdWithLignes(@Param("userId") Long userId);

  @Query("SELECT c FROM CommandeEntity c LEFT JOIN FETCH c.lignesCommande")
  List<CommandeEntity> findAllWithLignes();

  @Query("SELECT SUM(c.total) FROM CommandeEntity c")
  Double findTotalCommandes();

  @Query("SELECT SUM(c.total) FROM CommandeEntity c WHERE c.statut = :statut")
  Double findTotalCommandesByStatut(StatutCommande statut);

  @Query("SELECT COUNT(c) FROM CommandeEntity c")
  Long countCommandes();

  List<CommandeEntity> findAllByDateBetween(LocalDateTime startDate, LocalDateTime endDate);

}

