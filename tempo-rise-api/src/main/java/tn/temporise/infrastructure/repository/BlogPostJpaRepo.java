package tn.temporise.infrastructure.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.temporise.domain.model.BlogPostStatus;
import tn.temporise.infrastructure.persistence.entity.BlogPostEntity;

import java.util.Optional;

@Repository
public interface BlogPostJpaRepo extends JpaRepository<BlogPostEntity, Long> {
  @Query("SELECT c FROM BlogPostEntity c WHERE c.id = :id")
  Optional<BlogPostEntity> findById(@Param("id") Long id);

  @Modifying
  @Transactional
  @Query("DELETE FROM BlogPostEntity c WHERE c.id = :id")
  void deleteById(@Param("id") Long id);


  @Query("SELECT COUNT(b) FROM BlogPostEntity b WHERE b.status = :status")
  long countByStatus(@Param("status") BlogPostStatus status);

}
