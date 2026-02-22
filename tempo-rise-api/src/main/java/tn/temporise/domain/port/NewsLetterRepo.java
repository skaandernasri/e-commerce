package tn.temporise.domain.port;

import tn.temporise.domain.model.NewsLetter;
import tn.temporise.domain.model.NewsLetterStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface NewsLetterRepo {
  NewsLetter save(NewsLetter newsLetter);

  NewsLetter findById(Long id);

  List<NewsLetter> findAll();

  void deleteById(Long id);

  void deleteAll();

  NewsLetter findByEmail(String email);

  boolean existsByEmail(String email);

  List<NewsLetter> findBySubscriptionDateBetween(LocalDateTime startDate, LocalDateTime endDate);

  List<NewsLetter> findByStatus(NewsLetterStatus status);
}
