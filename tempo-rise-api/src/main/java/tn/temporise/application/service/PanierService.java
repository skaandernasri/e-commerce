package tn.temporise.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.client.ConflictException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.mapper.PanierMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.*;

import java.time.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PanierService {

  private final PanierRepo panierRepo;
  private final PanierMapper panierMapper;
  private final VariantRepo variantRepo;
  private final ExceptionFactory exceptionFactory;
  private final UserRepo userRepo;
  private final PanierVariantRepo panierVariantRepo;
  private final ProductRepo productRepo;
  private final StockService stockService;

  @Value("${reservedItem.expiration}")
  private Duration reservedItemExpiration;

  // === 🛒 Public Methods ===

  public PanierResponse getPanierById(Long id) {
    if (id == null)
      throw exceptionFactory.badRequest("badrequest.invalid_id");
    Panier panier = panierRepo.findById(id);
    return enrichWithVariants(panier);
  }

  public List<PanierResponse> getAllPaniers() {
    return panierRepo.findAll().stream().map(panierMapper::modelToResponse)
        .collect(Collectors.toList());
  }

  public void deletePanier(Long id) {
    if (id == null)
      throw exceptionFactory.badRequest("badrequest.invalid_id");
    panierRepo.findById(id);
    panierRepo.deleteById(id);
  }

  public void deleteAllPaniers() {
    panierRepo.deleteAll();
  }

  public void removeAllItemsFromPanier(Long userId, Long panierId) {
    userRepo.findById(userId);
    panierRepo.findByUtilisateurId(userId);
    panierVariantRepo.deleteByPanierId(panierId);
  }

  public PanierResponse getOrCreateCart(Long userId) {
    try {
      return enrichWithVariants(panierRepo.findByUtilisateurId(userId));
    } catch (NotFoundException e) {
      Panier newPanier =
          panierRepo.save(Panier.builder().utilisateur(userRepo.findById(userId)).build());
      return panierMapper.modelToResponse(newPanier);
    }
  }

  @Transactional
  public PanierResponse addItem(Long userId, Long variantId, CartItemRequest item) {
    validateCartInput(item);

    userRepo.findById(userId);
    Variant variant = variantRepo.findByIdForUpdate(variantId);
    Panier panier = getOrCreatePanier(userId);
    try {
      stockService.verifyStockAvailability(variant, item.getQuantite());
    } catch (ConflictException e) {
      throw exceptionFactory.conflict("conflict.insufficient_stock");
    }
    try {
      PanierVariant existing = panierVariantRepo.findByPanierIdAndVariantId(panier.id(), variantId);
      panierVariantRepo.save(existing.toBuilder().quantite(existing.quantite() + item.getQuantite())
          .expirationDate(generateExpirationDate()).build());
    } catch (NotFoundException e) {
      panierVariantRepo.save(PanierVariant.builder().panier(panier).variant(variant)
          .quantite(item.getQuantite()).expirationDate(generateExpirationDate()).build());
    }

    return enrichWithVariants(panier);
  }

  @Transactional
  public PanierResponse updateItemQuantity(Long userId, Long variantId, CartItemRequest item) {
    validateCartInput(item);

    userRepo.findById(userId);
    Variant variant = variantRepo.findById(variantId);
    Panier panier = panierRepo.findByUtilisateurId(userId);
    PanierVariant panierVariant =
        panierVariantRepo.findByPanierIdAndVariantId(panier.id(), variantId);

    long difference = item.getQuantite() - panierVariant.quantite();
    if (difference > 0)
      stockService.verifyStockAvailability(variant, difference);

    panierVariantRepo.save(panierVariant.toBuilder().quantite(item.getQuantite())
        .expirationDate(generateExpirationDate()).build());

    return enrichWithVariants(panier);
  }

  public void removeItem(Long userId, Long variantId) {
    userRepo.findById(userId);
    Panier panier = panierRepo.findByUtilisateurId(userId);
    PanierVariant panierVariant =
        panierVariantRepo.findByPanierIdAndVariantId(panier.id(), variantId);
    panierVariantRepo.deleteById(panierVariant.id());
  }

  @Transactional
  public PanierResponse mergeCart(Long userId, List<CartItemRequest> anonymousItems) {
    userRepo.findById(userId);
    Panier panier = getOrCreatePanier(userId);

    for (CartItemRequest item : anonymousItems) {
      if (item.getQuantite() <= 0)
        continue;

      Variant variant = variantRepo.findById(item.getId());
      stockService.verifyStockAvailability(variant, item.getQuantite());

      try {
        PanierVariant existing =
            panierVariantRepo.findByPanierIdAndVariantId(panier.id(), variant.id());
        panierVariantRepo.save(existing.toBuilder().quantite(item.getQuantite()).build());
      } catch (NotFoundException e) {
        panierVariantRepo.save(PanierVariant.builder().panier(panier).variant(variant)
            .quantite(item.getQuantite()).expirationDate(generateExpirationDate()).build());
      }
    }

    return enrichWithVariants(panier);
  }

  // === 🧹 Maintenance ===

  @Scheduled(fixedRate = 60000)
  protected void clearExpiredReservations() {
    LocalDateTime now = LocalDateTime.now();
    log.info("Clearing expired reservations at {}", now);
    panierVariantRepo.clearExpiredReservations(now);
  }

  // === 🧩 Helper Methods ===

  private void validateCartInput(CartItemRequest item) {
    if (item == null || item.getQuantite() <= 0)
      throw exceptionFactory.badRequest("badrequest.invalid_input");
  }

  private Panier getOrCreatePanier(Long userId) {
    try {
      return panierRepo.findByUtilisateurId(userId);
    } catch (NotFoundException e) {
      return panierRepo.save(Panier.builder().utilisateur(userRepo.findById(userId)).build());
    }
  }

  private LocalDateTime generateExpirationDate() {
    return LocalDateTime.now().plus(reservedItemExpiration);
  }

  private PanierResponse enrichWithVariants(Panier panier) {
    Set<Variant> variants = panierVariantRepo.findByPanierId(panier.id()).stream().map(pv -> {
      Variant variant = variantRepo.findById(pv.variant().id());
      return variant.toBuilder().reservedQuantity(pv.quantite())
          .availableQuantity(stockService.getAvailableStock(variant))
          .produit(productRepo.findById(variant.produit().id())).build();
    }).collect(Collectors.toSet());

    return panierMapper.modelToResponse(panier.toBuilder().variants(variants).build());
  }
}
