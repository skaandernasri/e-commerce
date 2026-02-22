package tn.temporise.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import tn.temporise.application.mapper.NewsLetterMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.NewsLetter;
import tn.temporise.domain.model.NewsLetterStatus;
import tn.temporise.domain.port.NewsLetterRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Slf4j
@RequiredArgsConstructor
public class NewsLetterRepoImp implements NewsLetterRepo {
  private final ExceptionFactory exceptionFactory;
  private final NewsLetterJpaRepo newsLetterJpaRepo;
  private final NewsLetterMapper newsLetterMapper;

  @Override
  public NewsLetter save(NewsLetter newsLetter) {
    return newsLetterMapper
        .entityToModel(newsLetterJpaRepo.save(newsLetterMapper.modelToEntity(newsLetter)));
  }

  @Override
  public NewsLetter findById(Long id) {
    return newsLetterMapper.entityToModel(newsLetterJpaRepo.findById(id)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.newsLetter")));
  }

  @Override
  public List<NewsLetter> findAll() {
    return newsLetterJpaRepo.findAll().stream().map(newsLetterMapper::entityToModel)
        .collect(Collectors.toList());
  }

  @Override
  public void deleteById(Long id) {
    newsLetterJpaRepo.deleteById(id);
  }

  @Override
  public void deleteAll() {
    newsLetterJpaRepo.deleteAll();
  }

  @Override
  public NewsLetter findByEmail(String email) {
    return newsLetterMapper.entityToModel(newsLetterJpaRepo.findByEmail(email)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.newsLetter")));
  }

  @Override
  public boolean existsByEmail(String email) {
    return newsLetterJpaRepo.existsByEmail(email);
  }

  @Override
  public List<NewsLetter> findBySubscriptionDateBetween(LocalDateTime startDate,
      LocalDateTime endDate) {
    return newsLetterJpaRepo.findBySubscribedAtBetween(startDate, endDate).stream()
        .map(newsLetterMapper::entityToModel).collect(Collectors.toList());
  }

  @Override
  public List<NewsLetter> findByStatus(NewsLetterStatus status) {
    return newsLetterJpaRepo.findByStatus(status).stream().map(newsLetterMapper::entityToModel)
        .collect(Collectors.toList());
  }
}
