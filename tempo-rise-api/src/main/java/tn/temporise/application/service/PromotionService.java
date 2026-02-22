package tn.temporise.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.application.mapper.PromotionMapper;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.ProductRepo;
import tn.temporise.domain.port.PromotionRepo;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PromotionService {
  private final PromotionRepo promotionRepo;
  private final PromotionMapper promotionMapper;
  private final ExceptionFactory exceptionFactory;
  private final ProductRepo productRepo;
  private final NotifService notifService;

  public PromotionResponse createPromotion(PromotionRequest promotionRequest) {
    try {
      if (promotionRequest == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_request");
      }

      Promotion promotion = promotionMapper.dtoToModel(promotionRequest);
      log.info("Promotion: {}", promotion);
      validateDate(promotion.dateDebut(), promotion.dateFin());

      Produit produit = productRepo.findById(promotionRequest.getProduitId());

      hasActivePromotions(produit, promotion);
      if (promotion.type().equals(PromotionType.FIXED) && (promotion.reduction() >= produit.prix()))
        throw exceptionFactory.badRequest("badrequest.invalid_promotion");
      if (promotion.type().equals(PromotionType.PERCENTAGE)
          && (promotion.reduction() >= 100 || promotion.reduction() <= 0))
        throw exceptionFactory.badRequest("badrequest.invalid_promotion");

      Promotion savedPromotion = promotionRepo.save(promotion.toBuilder().produit(produit).build());
      log.info("Promotion created: {}", savedPromotion);
      notifService.notifyPromotion(savedPromotion);
      return promotionMapper.modelToResponse(savedPromotion);
    } catch (NotFoundException | BadRequestException e) {
      log.warn("Promotion creation failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Promotion creation failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public PromotionResponse getPromotionById(Long id) {
    try {
      if (id == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }

      Promotion promotion = promotionRepo.findById(id);
      if (promotion == null) {
        throw exceptionFactory.notFound("notfound.promotion");
      }
      return promotionMapper.modelToResponse(promotion);
    } catch (BadRequestException | NotFoundException e) {
      log.warn("Promotion retrieval failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Promotion retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public List<PromotionResponse> getAllPromotions() {
    try {
      return promotionRepo.findAll().stream().map(promotionMapper::modelToResponse).toList();
    } catch (Exception e) {
      log.error("Promotion list retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public PromotionResponse updatePromotion(Long id, PromotionRequest promotionRequest) {
    try {
      if (id == null || promotionRequest == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }

      Promotion existingPromotion = promotionRepo.findById(id);
      if (existingPromotion == null) {
        throw exceptionFactory.notFound("notfound.promotion");
      }

      Produit produit = productRepo.findById(promotionRequest.getProduitId());
      if (produit == null) {
        throw exceptionFactory.notFound("notfound.product");
      }

      promotionRequest.setId(id);
      Promotion updatedPromotion = promotionMapper.dtoToModel(promotionRequest);
      validateDate(updatedPromotion.dateDebut(), updatedPromotion.dateFin());

      hasActivePromotions(produit, updatedPromotion);

      Promotion savedPromotion = promotionRepo.update(updatedPromotion);
      return promotionMapper.modelToResponse(savedPromotion);
    } catch (BadRequestException | NotFoundException e) {
      log.warn("Promotion update failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Promotion update failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public void deletePromotion(Long id) {
    try {
      if (id == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }

      promotionRepo.findById(id);
      promotionRepo.deleteById(id);
    } catch (BadRequestException | NotFoundException e) {
      log.warn("Promotion deletion failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Promotion deletion failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "Promotion deletion failed");
    }
  }

  public void deleteAllPromotions() {
    try {
      List<Promotion> promotionList = promotionRepo.findAll();
      if (promotionList.isEmpty()) {
        throw exceptionFactory.notFound("notfound.no_promotions");
      }
      promotionRepo.deleteAll();
    } catch (NotFoundException e) {
      log.warn("No promotions to delete", e);
      throw e;
    } catch (Exception e) {
      log.error("Promotion bulk deletion failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error",
          "Promotion bulk deletion failed");
    }
  }

  public List<PromotionResponse> getPromotionsByProduitId(Long produitId) {
    try {
      if (produitId == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }

      List<PromotionResponse> promotionList = promotionRepo.findByProduitId(produitId).stream()
          .map(promotionMapper::modelToResponse).toList();
      if (promotionList.isEmpty()) {
        throw exceptionFactory.notFound("notfound.no_promotions");
      }
      return promotionList;
    } catch (BadRequestException | NotFoundException e) {
      log.warn("Product promotions retrieval failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Product promotions retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public List<PromotionResponse> getActivePromotions() {
    try {
      return promotionRepo.findActivePromotions(LocalDateTime.now()).stream()
          .map(promotionMapper::modelToResponse).toList();
    } catch (NotFoundException e) {
      log.warn("Failed to retrieve active promotions", e);
      throw e;
    } catch (Exception e) {
      log.error("Failed to retrieve active promotions", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public List<PromotionResponse> getInactivePromotions() {
    try {
      // ZonedDateTime now=zoneConfig.toZonedDateTime(LocalDateTime.now());
      return promotionRepo.findInActivePromotions(LocalDateTime.now()).stream()
          .map(promotionMapper::modelToResponse).toList();
    } catch (NotFoundException e) {
      log.warn("Failed to retrieve inactive promotions", e);
      throw e;
    } catch (Exception e) {
      log.error("Failed to retrieve inactive promotions", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public void validateDate(LocalDateTime dateDebut, LocalDateTime dateFin)
      throws BadRequestException {
    if (dateDebut == null || dateFin == null) {
      throw exceptionFactory.badRequest("badrequest.null_dates");
    }
    if (dateDebut.isAfter(dateFin)) {
      throw exceptionFactory.badRequest("badrequest.invalid_date_range");
    }
    if (dateFin.isBefore(LocalDateTime.now())) {
      throw exceptionFactory.badRequest("badrequest.invalid_date_range");
    }
  }

  private void hasActivePromotions(Produit product, Promotion promotion) {
    if (product == null || promotion == null) {
      throw exceptionFactory.badRequest("badrequest.invalid_arguments");
    }

    if (product.promotions().isEmpty()) {
      return;
    }

    boolean hasOverlappingPromotion = product.promotions().stream()
        .filter(existingProm -> !existingProm.id().equals(promotion.id()))
        .anyMatch(existingProm -> isDateRangeOverlapping(existingProm.dateDebut(),
            existingProm.dateFin(), promotion.dateDebut(), promotion.dateFin()));

    if (hasOverlappingPromotion) {
      throw exceptionFactory.badRequest("badrequest.product_has_active_promotions");
    }
  }

  private boolean isDateRangeOverlapping(LocalDateTime start1, LocalDateTime end1,
      LocalDateTime start2, LocalDateTime end2) {
    return !start1.isAfter(end2) && !end1.isBefore(start2);
  }
}
