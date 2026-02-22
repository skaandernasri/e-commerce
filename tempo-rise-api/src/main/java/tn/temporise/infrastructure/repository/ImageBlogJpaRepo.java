package tn.temporise.infrastructure.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.temporise.infrastructure.persistence.entity.ImageBlogPostEntity;

import java.util.List;
import java.util.Optional;

public interface ImageBlogJpaRepo extends JpaRepository<ImageBlogPostEntity, Long> {
  @Query("SELECT c FROM ImageBlogPostEntity c WHERE c.id = :id")
  Optional<ImageBlogPostEntity> findById(@Param("id") Long id);

  @Modifying
  @Transactional
  @Query("DELETE FROM ImageBlogPostEntity c WHERE c.id = :id")
  void deleteById(@Param("id") Long id);

  @Query("SELECT i FROM ImageBlogPostEntity i WHERE i.blogPost.id = :id")
  Optional<List<ImageBlogPostEntity>> findByProduitId(@Param("id") Long id);
}
