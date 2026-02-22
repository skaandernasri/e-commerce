package tn.temporise.infrastructure.repository;

import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.temporise.infrastructure.persistence.entity.VariantEntity;

import java.util.Optional;


@Repository
public interface VariantJpaRepo extends JpaRepository<VariantEntity, Long> {
  boolean existsByColorAndSizeAndProduit_Id(String color, String size, Long produitId);

  @Modifying
  @Transactional
  @Query("DELETE FROM VariantEntity c WHERE c.id = :id")
  void deleteById(@Param("id") Long id);

  Long countByQuantityGreaterThan(Long stock);

  Long countByQuantityLessThanEqual(Long stock);

  Long countByQuantityEquals(Long stock);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT v FROM VariantEntity v WHERE v.id = :id")
  Optional<VariantEntity> findByIdWithLock(@Param("id") Long id);



}
