package tn.temporise.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import tn.temporise.application.mapper.AvisMapper;

import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.Avis;

import tn.temporise.domain.model.AvisFilter;
import tn.temporise.domain.port.AvisRepo;

import tn.temporise.infrastructure.persistence.entity.AvisEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class AvisRepoImp implements AvisRepo {
  private final AvisJpaRepo avisJpaRepo;
  private final AvisMapper avisMapper;
  private final ExceptionFactory exceptionFactory;

  @Override
  public Avis save(Avis avis) {
    AvisEntity savedEntity = avisJpaRepo.save(avisMapper.modelToEntity(avis));
    return avisMapper.entityToModel(savedEntity); // Convert saved entity back to domain model
  }

  @Override
  public Avis findById(Long id) {
    log.info("Finding Avis by ID: {}", id);
    Optional<AvisEntity> avisEntity = avisJpaRepo.findById(id);
    return avisEntity.map(avisMapper::entityToModel)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.avis")); // Convert entity to domain
                                                                        // model if found
  }

  @Override
  public List<Avis> findAll() {
    log.info("Fetching all Avis");
    return avisJpaRepo.findAll().stream().map(avisMapper::entityToModel)
        .collect(Collectors.toList());

  }

  @Override
  public Avis update(Avis avis) {
    log.info("Updating Avis: {}", avis);
    if (avis.id() == null || !avisJpaRepo.existsById(avis.id())) {
      log.warn("Avis with ID {} not found for update", avis.id());
      throw exceptionFactory.notFound("notfound.avis"); // Return null if the Avis does not exist
    }
    AvisEntity avisEntity = avisMapper.modelToEntity(avis); // Convert domain model to entity
    AvisEntity updatedEntity = avisJpaRepo.save(avisEntity); // Save updated entity
    return avisMapper.entityToModel(updatedEntity); // Convert updated entity back to domain model
  }

  @Override
  public void deleteById(Long id) {
    log.info("Deleting Avis by ID: {}", id);
    avisJpaRepo.deleteById(id); // Delete entity by ID
  }

  @Override
  public void deleteAll() {
    log.info("Deleting all Avis");
    avisJpaRepo.deleteAll(); // Delete all entities
  }

  @Override
  public List<Avis> findByProduitId(Long id) {
    log.info("Finding Avis by Produit ID: {}", id);
    List<AvisEntity> avisEntities = avisJpaRepo.findByProduitId(id).orElse(null); // Assuming
                                                                                  // findByProduitId
                                                                                  // is defined in
                                                                                  // AvisJpaRepo
    if (avisEntities == null)
      throw exceptionFactory.notFound("notfound.no_avis");
    return avisEntities.stream().map(avisMapper::entityToModel) // Convert each entity to domain
                                                                // model
        .collect(Collectors.toList());
  }

  @Override
  public List<Avis> findByUtilisateurId(Long id) {
    log.info("Finding Avis by Utilisateur ID: {}", id);
    List<AvisEntity> avisEntities = avisJpaRepo.findByUtilisateurId(id)
        .orElseThrow(() -> exceptionFactory.notFound("notfound.no_avis")); // Assuming
                                                                           // findByUtilisateurId is
                                                                           // defined in AvisJpaRepo
    return avisEntities.stream().map(avisMapper::entityToModel) // Convert each entity to domain
                                                                // model
        .collect(Collectors.toList());
  }

  @Override
  public Page<Avis> findAllByProduitId(Long id, Pageable pageable) {
    log.info("Finding Avis by Produit ID: {}", pageable);
    Page<AvisEntity> avisEntities = avisJpaRepo.findAllByProduit_Id(id, pageable); // Assuming
                                                                                   // findByProduitId
                                                                                   // is defined in
                                                                                   // AvisJpaRepo

    return avisEntities.map(avisMapper::entityToModel);
  }

  @Override
  public Page<Avis> findAll(AvisFilter filter, Pageable pageable) {
    Specification<AvisEntity> spec = Specification.where(null);

    if (filter.productName() != null) {
      spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("produit").get("nom")),
          "%" + filter.productName().toLowerCase() + "%"));
    }
    if (filter.userEmail() != null && filter.content() != null) {
      spec = spec.and((root, query, cb) -> cb.or(
          cb.like(cb.lower(root.get("utilisateur").get("email")),
              "%" + filter.userEmail().toLowerCase() + "%"),
          cb.like(cb.lower(root.get("commentaire")), "%" + filter.content().toLowerCase() + "%")));
    } else {
      if (filter.userEmail() != null) {
        spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("utilisateur").get("email")),
            "%" + filter.userEmail().toLowerCase() + "%"));
      }
      if (filter.content() != null) {
        spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("commentaire")),
            "%" + filter.content().toLowerCase() + "%"));
      }
    }
    log.debug("Executing query with spec: {}", spec);
    return avisJpaRepo.findAll(spec, pageable).map(avisMapper::entityToModel);
  }

  @Override
  public Float findAverageRating() {
    return avisJpaRepo.findAverageRating();
  }

  @Override
  public Long findReviewsInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
    return avisJpaRepo.findReviewsInPeriod(startDate, endDate);
  }

  @Override
  public Long findReviewsInPeriodByProduitName(LocalDateTime startDate, LocalDateTime endDate,
      String productName) {
    return avisJpaRepo.findReviewsInPeriodByProduitName(startDate, endDate, productName);
  }

  @Override
  public Float findAverageRatingInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
    return avisJpaRepo.findAverageRatingInPeriod(startDate, endDate);
  }

  @Override
  public Float findAverageRatingInPeriodByProduitName(LocalDateTime startDate,
      LocalDateTime endDate, String productName) {
    return avisJpaRepo.findAverageRatingInPeriodByProduitName(startDate, endDate, productName);
  }

  @Override
  public Float findAverageRatingByProduitName(String nom) {
    return avisJpaRepo.findAverageRatingByProduitId(nom);
  }



}
