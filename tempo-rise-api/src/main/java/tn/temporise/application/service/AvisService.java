package tn.temporise.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.application.mapper.AvisMapper;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.AvisRepo;
import tn.temporise.domain.port.ProductRepo;
import tn.temporise.domain.port.UserRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AvisService {
  private final AvisRepo avisRepo;
  private final AvisMapper avisMapper;
  private final UserRepo userRepo;
  private final ProductRepo productRepo;
  private final ExceptionFactory exceptionFactory;

  public AvisResponse createAvis(AvisRequest avisRequest) {
    try {
      if (avisRequest == null || avisRequest.getNote() == null
          || avisRequest.getCommentaire() == null || avisRequest.getUtilisateurId() == null
          || avisRequest.getProduitId() == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_request");
      }
      // These will throw NotFoundException automatically
      userRepo.findById(avisRequest.getUtilisateurId());
      productRepo.findById(avisRequest.getProduitId());

      Avis avis = avisMapper.dtoToModel(avisRequest);
      Avis savedAvis = avisRepo.save(avis);

      return avisMapper.modelToResponse(savedAvis);

    } catch (BadRequestException | NotFoundException e) {
      log.warn("Avis creation failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Avis creation failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "Avis creation failed" + e.getMessage());
    }
  }

  public AvisResponse getAvisById(Long id) {
    try {
      if (id == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }

      Avis avis = avisRepo.findById(id);

      return avisMapper.modelToResponse(avis);

    } catch (BadRequestException | NotFoundException e) {
      log.warn("Avis retrieval failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Avis retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "Avis retrieval failed" + e.getMessage());
    }
  }

  public List<AvisResponse> getAllAvis() {
    try {

      return avisRepo.findAll().stream().map(avisMapper::modelToResponse).toList();

    } catch (NotFoundException e) {
      log.warn("No avis found: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Avis list retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "Avis list retrieval failed" + e.getMessage());
    }
  }

  public List<AvisResponse> getAvisByProduitId(Long produitId) {
    try {
      if (produitId == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }

      productRepo.findById(produitId);


      return avisRepo.findByProduitId(produitId).stream().map(avisMapper::modelToResponse).toList();

    } catch (BadRequestException | NotFoundException e) {
      log.warn("Product avis retrieval failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Product avis retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "Product avis retrieval failed" + e.getMessage());
    }
  }

  public void deleteAllAvis() {
    try {
      List<AvisResponse> avisList =
          avisRepo.findAll().stream().map(avisMapper::modelToResponse).toList();

      avisRepo.deleteAll();

    } catch (NotFoundException e) {
      log.warn("No avis to delete", e);
      throw e;
    } catch (Exception e) {
      log.error("Avis bulk deletion failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "Avis bulk deletion failed" + e.getMessage());
    }
  }

  public void deleteAvis(Long id) {
    try {
      if (id == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }

      avisRepo.findById(id);
      avisRepo.deleteById(id);

    } catch (BadRequestException | NotFoundException e) {
      log.warn("Avis deletion failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Avis deletion failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "Avis deletion failed" + e.getMessage());
    }
  }

  public List<AvisResponse> getAvisByUtilisateurId(Long id) {
    try {
      if (id == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }

      userRepo.findById(id);

      return avisRepo.findByUtilisateurId(id).stream().map(avisMapper::modelToResponse).toList();

    } catch (BadRequestException | NotFoundException e) {
      log.warn("User avis retrieval failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("User avis retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "User avis retrieval failed" + e.getMessage());
    }
  }

  public AvisResponse updateAvis(Long id, AvisRequest avisRequest) {
    try {
      if (id == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }

      avisRepo.findById(id);

      avisRequest.setId(id);
      Avis avis = avisMapper.dtoToModel(avisRequest);
      Avis savedAvis = avisRepo.save(avis);
      return avisMapper.modelToResponse(savedAvis);

    } catch (BadRequestException | NotFoundException e) {
      log.warn("Avis update failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Avis update failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "Avis update failed " + e.getMessage());
    }
  }

  public GetAvisByProductIdPaged200Response getAllAvisByProductId(Long id, int page, int size) {
    try {
      productRepo.findById(id);
      page = Math.max(page - 1, 0);
      Page<Avis> avisList = avisRepo.findAllByProduitId(id,
          PageRequest.of(page, size, Sort.by("datePublication").descending()));
      // avisList.get
      GetAvisByProductIdPaged200Response response = new GetAvisByProductIdPaged200Response();

      response.setTotalPages(avisList.getTotalPages());
      response.setTotalElements(avisList.getTotalElements());
      response.setContent(avisList.getContent().stream().map(avisMapper::modelToResponse).toList());
      return response;
    } catch (NotFoundException e) {
      log.warn("No avis found: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Avis list retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "Avis list retrieval failed" + e.getMessage());
    }
  }

  @Transactional
  public GetAvisPaged200Response getAllAvisPaged(String productName, String userEmail,
      String comment, int page, int size) {
    try {
      // Validate input parameters
      page = Math.max(page - 1, 0);
      size = Math.max(size, 1);

      // Prepare statistics variables
      float roundedAverage;
      long reviewsThisWeek;
      long reviewsWeekAgo;
      float averageRatingsThisWeek;
      float averageRatingsWeekAgo;



      log.debug("Fetching avis with filter - productName: {}, userEmail: {}, comment: {}",
          productName, userEmail, comment);

      AvisFilter filter = new AvisFilter(productName, userEmail, comment);

      // Fetch paginated results
      Page<Avis> avisList = avisRepo.findAll(filter,
          PageRequest.of(page, size, Sort.by("datePublication").descending()));

      log.debug("Found {} items ({} total)", avisList.getNumberOfElements(),
          avisList.getTotalElements());

      // Calculate statistics based on whether we're filtering by product
      LocalDateTime now = LocalDateTime.now();
      LocalDateTime oneWeekAgo = now.minusWeeks(1);
      LocalDateTime twoWeeksAgo = now.minusWeeks(2);

      if (StringUtils.hasText(filter.productName())) {
        // Product-specific statistics
        roundedAverage = roundToTwoDecimals(Optional
            .ofNullable(avisRepo.findAverageRatingByProduitName(filter.productName())).orElse(0f));

        reviewsThisWeek = Optional
            .ofNullable(
                avisRepo.findReviewsInPeriodByProduitName(oneWeekAgo, now, filter.productName()))
            .orElse(0L);

        reviewsWeekAgo = Optional.ofNullable(avisRepo.findReviewsInPeriodByProduitName(twoWeeksAgo,
            oneWeekAgo, filter.productName())).orElse(0L);

        averageRatingsThisWeek = Optional.ofNullable(
            avisRepo.findAverageRatingInPeriodByProduitName(oneWeekAgo, now, filter.productName()))
            .orElse(0f);

        averageRatingsWeekAgo =
            Optional.ofNullable(avisRepo.findAverageRatingInPeriodByProduitName(twoWeeksAgo,
                oneWeekAgo, filter.productName())).orElse(0f);
      } else {
        // Global statistics
        roundedAverage =
            roundToTwoDecimals(Optional.ofNullable(avisRepo.findAverageRating()).orElse(0f));

        reviewsThisWeek =
            Optional.ofNullable(avisRepo.findReviewsInPeriod(oneWeekAgo, now)).orElse(0L);

        reviewsWeekAgo =
            Optional.ofNullable(avisRepo.findReviewsInPeriod(twoWeeksAgo, oneWeekAgo)).orElse(0L);

        averageRatingsThisWeek =
            Optional.ofNullable(avisRepo.findAverageRatingInPeriod(oneWeekAgo, now)).orElse(0f);

        averageRatingsWeekAgo = Optional
            .ofNullable(avisRepo.findAverageRatingInPeriod(twoWeeksAgo, oneWeekAgo)).orElse(0f);
      }

      // Map and return response
      return new GetAvisPaged200Response().totalPages(avisList.getTotalPages())
          .totalElements(avisList.getTotalElements()).averageRating(roundedAverage)
          .reviewsThisWeek(reviewsThisWeek).reviewsWeekAgo(reviewsWeekAgo)
          .averageRatingsThisWeek(averageRatingsThisWeek)
          .averageRatingsWeekAgo(averageRatingsWeekAgo)
          .content(avisList.getContent().stream().map(avisMapper::modelToResponse).toList());

    } catch (Exception e) {
      log.error("Query failed with filter - productName: {}, userEmail: {}, comment: {}",
          productName, userEmail, comment, e);
      throw exceptionFactory.internalServerError("query.failed",
          "Failed to retrieve avis: " + e.getMessage());
    }
  }

  private float roundToTwoDecimals(float value) {
    return Math.round(value * 100f) / 100f;
  }

}

