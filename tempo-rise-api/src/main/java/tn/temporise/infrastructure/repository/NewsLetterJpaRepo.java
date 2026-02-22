package tn.temporise.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.temporise.domain.model.NewsLetterStatus;
import tn.temporise.infrastructure.persistence.entity.NewsLetterEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NewsLetterJpaRepo extends JpaRepository<NewsLetterEntity, Long> {
  List<NewsLetterEntity> findBySubscribedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

  List<NewsLetterEntity> findByStatus(NewsLetterStatus status);

  Optional<NewsLetterEntity> findByEmail(String email);

  Boolean existsByEmail(String email);
}
