package tn.temporise.infrastructure.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.temporise.infrastructure.persistence.entity.WhishlistItemEntity;

import java.util.List;

@Repository
public interface WhishlistItemJpaRepo extends JpaRepository<WhishlistItemEntity, Long> {
  List<WhishlistItemEntity> findAllByWhishlistId(Long whishlistId);

  @Transactional
  void deleteAllByWhishlistUtilisateurId(Long userId);

  @Transactional
  void deleteByWhishlistUtilisateurIdAndProduitId(Long userId, Long produitId);

  boolean existsByWhishlistIdAndProduitId(Long whishlistId, Long produitId);
}
