package tn.temporise.infrastructure.repository;

import feign.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.temporise.domain.model.TypePage;
import tn.temporise.domain.model.TypeSection;
import tn.temporise.infrastructure.persistence.entity.SectionEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectionJpaRepo extends JpaRepository<SectionEntity, Long> {
  Optional<SectionEntity> findByTitre(String titre);

  List<SectionEntity> findByType(TypeSection type);

  List<SectionEntity> findByTypePage(TypePage typePage);

  @Modifying
  @Transactional
  @Query("UPDATE SectionEntity s SET s.active = false WHERE s.type = :type AND typePage = :typePage")
  void checkAllTypeInactive(@Param("type") TypeSection type, @Param("typePage") TypePage typePage);
}

