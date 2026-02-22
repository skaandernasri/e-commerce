package tn.temporise.infrastructure.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.temporise.infrastructure.persistence.entity.ImageProduitEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageProduitJpaRepo extends JpaRepository<ImageProduitEntity, Long> {
  @Query("SELECT c FROM ImageProduitEntity c WHERE c.id = :id")
  Optional<ImageProduitEntity> findById(@Param("id") Long id);

  @Modifying
  @Transactional
  @Query("DELETE FROM ImageProduitEntity c WHERE c.id = :id")
  void deleteById(@Param("id") Long id);

  @Query("SELECT i FROM ImageProduitEntity i WHERE i.produit.id = :id")
  Optional<List<ImageProduitEntity>> findByProduitId(@Param("id") Long id);
}
