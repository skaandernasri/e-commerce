package tn.temporise.infrastructure.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.temporise.infrastructure.persistence.entity.CodePromoEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CodePromoJpaRepo extends JpaRepository<CodePromoEntity, Long> {
  @Query("SELECT c FROM CodePromoEntity c WHERE c.id = :id")
  Optional<CodePromoEntity> findById(@Param("id") Long id);

  @Modifying
  @Transactional
  @Query("DELETE FROM CodePromoEntity c WHERE c.id = :id")
  void deleteById(@Param("id") Long id);

  Optional<CodePromoEntity> findByCode(String code);

  boolean existsByCode(String code);

  // inactive codes
  List<CodePromoEntity> findBydateExpirationLessThanEqual(LocalDateTime currentDate);

  // active codes
  List<CodePromoEntity> findBydateExpirationGreaterThan(LocalDateTime currentDate);
}
