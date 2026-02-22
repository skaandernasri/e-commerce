package tn.temporise.infrastructure.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.temporise.infrastructure.persistence.entity.FactureEntity;

import java.util.List;

@Repository
public interface FactureJpaRepo extends JpaRepository<FactureEntity, Long> {
  FactureEntity findByCommande_Id(Long id);

  @Query("SELECT f FROM FactureEntity f WHERE f.commande.user.id = :userId")
  List<FactureEntity> findByUserId(@Param("userId") Long userId);

  @Modifying
  @Transactional
  @Query("DELETE FROM FactureEntity c WHERE c.id = :id")
  void deleteById(@Param("id") Long id);
}
