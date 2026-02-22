package tn.temporise.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.client.ConflictException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.PanierVariantRepo;
import tn.temporise.domain.port.VariantRepo;


@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

  private final ExceptionFactory exceptionFactory;
  private final VariantRepo variantRepo;
  private final PanierVariantRepo panierVariantRepo;

  /**
   * Updates stock quantities when a Commande (order) is created or returned.
   *
   * @param commande The order to process.
   * @param isReturn True if the order is being returned (stock restored), false if newly placed
   *        (stock reduced).
   */
  public void updateStockForCommande(Commande commande, boolean isReturn)
      throws ConflictException, NotFoundException {

    if (!isReturn) {
      verifyCommandeStockAvailability(commande);
      commande.lignesCommande().forEach(this::decreaseStock);
    } else {
      commande.lignesCommande().forEach(this::increaseStock);
    }
  }

  /**
   * Ensures all products in the order have sufficient stock before processing.
   */
  private void verifyCommandeStockAvailability(Commande commande) throws ConflictException {
    long currentUserId = commande.user().id();

    for (LigneCommande ligne : commande.lignesCommande()) {
      Variant variant = variantRepo.findByIdForUpdate(ligne.variant().id());

      long reservedQuantity =
          panierVariantRepo.findByVariantIdAndUserIdDifferent(variant.id(), currentUserId).stream()
              .mapToLong(PanierVariant::quantite).sum();

      long available = Math.max(variant.quantity() - reservedQuantity, 0);
      if (available < ligne.quantite()) {
        throw exceptionFactory.conflict("conflict.insufficient_stock");
      }
    }
  }

  /**
   * Checks if a given variant has enough available quantity.
   */
  public void verifyStockAvailability(Variant variant, long requestedQuantity) {
    if (getAvailableStock(variant) < requestedQuantity) {
      throw exceptionFactory.conflict("conflict.insufficient_stock");
    }
  }

  /**
   * Calculates currently available stock (excluding reserved quantities).
   */
  public long getAvailableStock(Variant variant) {
    return Math.max(variant.quantity() - getReservedQuantity(variant), 0);
  }

  /**
   * Calculates total reserved quantity for a variant (e.g., in shopping carts).
   */
  public long getReservedQuantity(Variant variant) {
    return panierVariantRepo.findByVariantId(variant.id()).stream()
        .mapToLong(PanierVariant::quantite).sum();
  }

  /**
   * Adjusts stock when a single order line is added or removed.
   */
  public void adjustStockForOrderLine(LigneCommande ligneCommande, boolean isDelete)
      throws NotFoundException {
    Variant variant = variantRepo.findByIdForUpdate(ligneCommande.variant().id());
    long updatedQuantity =
        variant.quantity() + (isDelete ? ligneCommande.quantite() : -ligneCommande.quantite());
    variantRepo.save(variant.toBuilder().quantity(updatedQuantity).build());
  }

  /**
   * Adjusts stock when an existing order line is updated (quantity changed).
   */
  public void adjustStockOnOrderLineUpdate(LigneCommande oldLine, LigneCommandeRequest newLine)
      throws NotFoundException {
    Variant variant = variantRepo.findByIdForUpdate(oldLine.variant().id());
    long delta = newLine.getQuantite() - oldLine.quantite(); // positive if increased
    long updatedQuantity = variant.quantity() - delta;
    variantRepo.save(variant.toBuilder().quantity(updatedQuantity).build());
  }

  /**
   * Validates that stock is sufficient when increasing an existing order line’s quantity.
   */
  public void validateStockForOrderLineUpdate(LigneCommande oldLine, LigneCommandeRequest newLine)
      throws NotFoundException {
    Variant variant = variantRepo.findByIdForUpdate(oldLine.variant().id());
    long additionalRequested = newLine.getQuantite() - oldLine.quantite();

    if (additionalRequested > 0) {
      long availableStock = getAvailableStock(variant);
      if (additionalRequested > availableStock) {
        throw exceptionFactory.conflict("conflict.insufficient_stock");
      }
    }
  }

  // === Private helper methods ===

  private void decreaseStock(LigneCommande ligneCommande) {
    Variant variant = variantRepo.findByIdForUpdate(ligneCommande.variant().id());
    variantRepo
        .save(variant.toBuilder().quantity(variant.quantity() - ligneCommande.quantite()).build());
  }

  private void increaseStock(LigneCommande ligneCommande) {
    Variant variant = variantRepo.findByIdForUpdate(ligneCommande.variant().id());
    variantRepo
        .save(variant.toBuilder().quantity(variant.quantity() + ligneCommande.quantite()).build());
  }
}
