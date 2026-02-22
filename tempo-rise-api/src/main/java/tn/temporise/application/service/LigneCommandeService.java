package tn.temporise.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.ConflictException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.mapper.CommandeMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.CommandeRepo;
import tn.temporise.domain.port.ConfigurationGlobalRepo;
import tn.temporise.domain.port.LigneCommandeRepo;
import tn.temporise.domain.port.VariantRepo;

@Slf4j
@Service
@RequiredArgsConstructor
public class LigneCommandeService {

  private final ExceptionFactory exceptionFactory;
  private final LigneCommandeRepo ligneCommandeRepo;
  private final CommandeMapper commandeMapper;
  private final CommandeRepo commandeRepo;
  private final VariantRepo variantRepo;
  private final StockService stockService;
  private final ConfigurationGlobalRepo configurationGlobalRepo;

  /**
   * Updates or adds a line item in a Commande, adjusting stock as needed.
   */
  @Transactional
  public CommandeResponse updateLigneCommande(Long commandeId, LigneCommandeRequest request) {
    try {
      validateRequest(commandeId, request);

      Commande commande = commandeRepo.findById(commandeId);
      Variant variant = variantRepo.findById(request.getVariantId());

      LigneCommande ligneCommande;
      boolean isNewLine = false;

      try {
        // Existing line: validate and adjust stock difference
        ligneCommande =
            ligneCommandeRepo.findByCommandeIdAndVariantId(commandeId, request.getVariantId());
        stockService.validateStockForOrderLineUpdate(ligneCommande, request);

        if (isStockManaged(commande)) {
          stockService.adjustStockOnOrderLineUpdate(ligneCommande, request);
        }

        ligneCommande = ligneCommande.toBuilder().prixTotal(request.getPrixTotal())
            .quantite(request.getQuantite()).build();
        ligneCommandeRepo.save(ligneCommande);

      } catch (NotFoundException e) {
        // New line: verify stock and insert
        isNewLine = true;
        stockService.verifyStockAvailability(variant, request.getQuantite());

        ligneCommande = LigneCommande.builder().commande(Commande.builder().id(commandeId).build())
            .variant(variant).quantite(request.getQuantite()).prixTotal(request.getPrixTotal())
            .build();
        ligneCommandeRepo.save(ligneCommande);

        if (isStockManaged(commande)) {
          stockService.adjustStockForOrderLine(ligneCommande, false);
        }
      }
      Commande savedCommande = commandeRepo.findById(commandeId);
      // Recalculate and update order total
      Commande updatedCommande = recalculateCommandeTotal(savedCommande, request.getShipping());
      log.info("Ligne commande {} successfully {}", ligneCommande.id(),
          isNewLine ? "added" : "updated");

      return commandeMapper.modelToDto(updatedCommande);

    } catch (BadRequestException | NotFoundException | ConflictException e) {
      log.warn("Ligne commande update failed: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error during ligne commande update", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  /**
   * Deletes a line item and restores stock if needed.
   */
  @Transactional
  public Response deleteLigneCommande(Long commandeId, Long variantId) {
    try {
      Commande commande = commandeRepo.findById(commandeId);
      variantRepo.findById(variantId);
      LigneCommande ligneCommande =
          ligneCommandeRepo.findByCommandeIdAndVariantId(commandeId, variantId);

      ligneCommandeRepo.deleteById(ligneCommande.id());
      double newTotal = commande.total() - ligneCommande.prixTotal();
      double oldTotal = commande.total();
      try {
        ConfigurationGlobal config = configurationGlobalRepo.getConfig();
        if (oldTotal >= config.seuilLivraisonGratuite()
            && newTotal < config.seuilLivraisonGratuite()) {
          newTotal += config.valeurLivraison();
        }
      } catch (NotFoundException ignore) {
      }
      Commande updatedCommande = commande.toBuilder().total(newTotal).build();
      updatedCommande = commandeRepo.save(updatedCommande);

      if (isStockManaged(updatedCommande)) {
        stockService.adjustStockForOrderLine(ligneCommande, true);
      }

      log.info("Ligne commande {} deleted successfully from commande {}", variantId, commandeId);
      Response response = new Response();
      response.setCode("200");
      response.setMessage("Item supprimé avec succès");
      return response;

    } catch (NotFoundException e) {
      throw e;
    } catch (Exception e) {
      log.error("Failed to delete ligne commande", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  // === Helper Methods ===

  private void validateRequest(Long id, LigneCommandeRequest request) {
    if (id == null || request == null || request.getQuantite() == null
        || request.getVariantId() == null) {
      throw exceptionFactory.badRequest("badrequest.invalid_input");
    }
  }

  private boolean isStockManaged(Commande commande) {
    return commande.statut() == StatutCommande.EN_COURS
        || commande.statut() == StatutCommande.EXPEDIEE;
  }

  private Commande recalculateCommandeTotal(Commande commande, double shipping) {
    double total = ligneCommandeRepo.findByCommandeId(commande.id()).stream()
        .mapToDouble(LigneCommande::prixTotal).sum() + shipping;
    Commande updated = commande.toBuilder().total(total).build();
    return commandeRepo.save(updated);
  }
}
