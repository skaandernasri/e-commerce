package tn.temporise.infrastructure.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.temporise.infrastructure.persistence.entity.ProduitEntity;

import java.util.List;
import java.util.Optional;


@Repository
public interface ProductJpaRepo
    extends JpaRepository<ProduitEntity, Long>, JpaSpecificationExecutor<ProduitEntity> {
  @Query("SELECT p FROM ProduitEntity p WHERE p.id = :id")
  Optional<ProduitEntity> findById(@Param("id") Long id);

  @Modifying
  @Transactional
  @Query("DELETE FROM ProduitEntity p WHERE p.id = :id")
  void deleteById(@Param("id") Long id);

  @Query("SELECT p FROM ProduitEntity p LEFT JOIN FETCH p.promotions WHERE p.id = :id")
  Optional<ProduitEntity> findByIdWithPromotions(@Param("id") Long id);

  List<ProduitEntity> findAllByIdInAndActif(@Param("ids") List<Long> ids,
      @Param("actif") Boolean actif);

  @Query("""
          SELECT COUNT(p.id)
          FROM ProduitEntity p
          WHERE (
              SELECT SUM(v.quantity)
              FROM VariantEntity v
              WHERE v.produit = p
          ) > :stock
      """)
  Long countProductsWithTotalQuantityGreaterThan(@Param("stock") Long stock);

  @Query("""
          SELECT COUNT(p.id)
          FROM ProduitEntity p
          WHERE (
              SELECT SUM(v.quantity)
              FROM VariantEntity v
              WHERE v.produit = p
          ) <= :stock
      """)
  Long countProductsWithTotalQuantityLessThanEqual(@Param("stock") Long stock);

  @Query("""
          SELECT COUNT(p.id)
          FROM ProduitEntity p
          WHERE (
              SELECT SUM(v.quantity)
              FROM VariantEntity v
              WHERE v.produit = p
          ) = :stock
      """)
  Long countProductsWithTotalQuantityEquals(@Param("stock") Long stock);

  @Query("SELECT MAX(p.prix) FROM ProduitEntity p where p.actif = true")
  Double getMaxPrice();
}
