package tn.temporise.application.service;

import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.auth.UnauthorizedException;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.ConflictException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.mapper.DateMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.application.mapper.CommandeMapper;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.*;
import tn.temporise.infrastructure.client.payment.PaymentApi;
import tn.temporise.infrastructure.security.utils.CookiesUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandeService {
  private final CommandeRepo commandeRepo;
  private final LigneCommandeRepo ligneCommandeRepo;
  private final UserRepo utilisateurRepo;
  private final CommandeMapper commandeMapper;
  private final ExceptionFactory exceptionFactory;
  private final CodePromoRepo codePromoRepo;
  private final NotifService notifService;
  private final VariantRepo variantRepo;
  private final FactureRepo factureRepo;
  private final EmailInterface emailInterface;
  private final StockService stockService;
  private final CookiesUtil cookiesUtil;
  private final UtilisateurAnonymeService utilisateurAnonymeService;
  private final AdresseRepo adresseRepo;
  private final PaymentService paymentService;
  private final PaiementRepo paiementRepo;
  private final PaymentApi paymentApi;


  /**
   * Créer une commande
   *
   * @param commandeRequest CommandeRequest
   * @return CommandeResponse
   */
  @Transactional
  public PaiementResponse createCommande(CommandeRequest commandeRequest) {
    try {

      if (commandeRequest == null || commandeRequest.getToken() == null
          || commandeRequest.getUtilisateurId() == null)
        throw exceptionFactory.badRequest("badrequest.invalid_request");
      utilisateurRepo.findById(commandeRequest.getUtilisateurId());
      log.info("commandeRequest : {}", commandeRequest);
      adresseRepo.findById(commandeRequest.getAdresseFacturationId());
      adresseRepo.findById(commandeRequest.getAdresseLivraisonId());
      Commande commande = commandeMapper.dtoToModel(commandeRequest);
      log.info("commande: {}", commande);

      // Handle code promo
      if (commande.codePromo() != null && commande.codePromo().code() != null) {
        log.info("codePromo: {}", commande.codePromo());
        CodePromo codePromo = codePromoRepo.findByCode(commande.codePromo().code().toUpperCase());
        commande = commande.toBuilder().codePromo(codePromo).build();
      } else {
        commande = commande.toBuilder().codePromo(null).build();
      }

      commande = commande.toBuilder().statut(StatutCommande.EN_ATTENTE)
          .total(commandeRequest.getTotal() + commandeRequest.getShipping()).build();

      // Save commande
      Commande savedCommande = commandeRepo.save(commande);
      log.info("Commande saved with ID: {}", savedCommande.id());

      // DEBUG: Check if commande was actually saved
      if (savedCommande.id() == null) {
        log.error("CRITICAL: Commande was not saved properly - ID is null!");
        throw exceptionFactory.internalServerError("internal.server_error",
            "Failed to save commande");
      }

      // Save ligne commandes
      commandeRequest.getProduits().forEach(produit -> {
        produit.getVariants().forEach(variant -> {
          // DEBUG: Verify variant exists
          Variant existingVariant = variantRepo.findById(variant.getId());
          log.info("Found variant with ID: {}", existingVariant.id());

          LigneCommande ligneCommande = LigneCommande.builder().commande(savedCommande)
              .quantite(variant.getReservedQuantity()).variant(existingVariant)
              .prixTotal(variant.getReservedQuantity() * produit.getNewPrice()).build();

          log.info("Attempting to save LigneCommande: {}", ligneCommande);
          LigneCommande savedLigne = ligneCommandeRepo.save(ligneCommande);
          log.info("LigneCommande saved with ID: {}", savedLigne.id());

          // DEBUG: Verify it was actually saved
          if (savedLigne.id() == null) {
            log.error("CRITICAL: LigneCommande was not saved properly!");
          }
        });
      });

      PaiementResponse paiementResponse = new PaiementResponse();
      // Handle payment
      if (savedCommande.modePaiement().equals(ModePaiement.CARTE_BANCAIRE)) {
        // Payment logic commented out
        paiementResponse = createCommandeOnline(commandeRequest.getToken(), savedCommande);
        // return null;
      } else {

        paiementResponse.setPaymentRef(savedCommande.id().toString());

      }
      processOrder(savedCommande, commandeRequest.getUtilisateurId());
      return paiementResponse;
    } catch (BadRequestException | NotFoundException | UnauthorizedException
        | ConflictException e) {
      log.warn("Commande creation failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Commande creation failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  @Transactional
  public PaiementResponse createCommandeAsGuest(CommandeGuestRequest commandeRequest) {
    try {
      if (commandeRequest == null || commandeRequest.getToken() == null)
        throw exceptionFactory.badRequest("badrequest.invalid_request");
      UtilisateurAnonymeResponse response = utilisateurAnonymeService
          .getOrCreateUtilisateurAnonyme(cookiesUtil.getUUIDCookieValue("anonyme_session_token"));
      assert response != null;
      commandeRequest.setUtilisateurId(response.getId());

      log.info("commandeRequest : {}", commandeRequest);
      adresseRepo.findById(commandeRequest.getAdresseFacturationId());
      adresseRepo.findById(commandeRequest.getAdresseLivraisonId());
      Commande commande = commandeMapper.dtoToModel(commandeRequest);
      log.info("commande: {}", commande);

      // Handle code promo
      if (commande.codePromo() != null && commande.codePromo().code() != null) {
        log.info("codePromo: {}", commande.codePromo());
        CodePromo codePromo = codePromoRepo.findByCode(commande.codePromo().code().toUpperCase());
        commande = commande.toBuilder().codePromo(codePromo).build();
      } else {
        commande = commande.toBuilder().codePromo(null).build();
      }

      commande = commande.toBuilder().statut(StatutCommande.EN_ATTENTE)
          .total(commandeRequest.getTotal() + commandeRequest.getShipping()).build();

      // Save commande
      Commande savedCommande = commandeRepo.save(commande);
      log.info("Commande saved with ID: {}", savedCommande.id());

      // DEBUG: Check if commande was actually saved
      if (savedCommande.id() == null) {
        log.error("CRITICAL: Commande was not saved properly - ID is null!");
        throw exceptionFactory.internalServerError("internal.server_error",
            "Failed to save commande");
      }

      // Save ligne commandes
      commandeRequest.getProduits().forEach(produit -> {
        produit.getVariants().forEach(variant -> {
          // DEBUG: Verify variant exists
          Variant existingVariant = variantRepo.findById(variant.getId());
          log.info("Found variant with ID: {}", existingVariant.id());

          LigneCommande ligneCommande = LigneCommande.builder().commande(savedCommande)
              .quantite(variant.getCartQuantity()).variant(existingVariant)
              .prixTotal(variant.getCartQuantity() * produit.getNewPrice()).build();

          log.info("Attempting to save LigneCommande: {}", ligneCommande);
          LigneCommande savedLigne = ligneCommandeRepo.save(ligneCommande);
          log.info("LigneCommande saved with ID: {}", savedLigne.id());

          // DEBUG: Verify it was actually saved
          if (savedLigne.id() == null) {
            log.error("CRITICAL: LigneCommande was not saved properly!");
          }
        });
      });

      PaiementResponse paiementResponse = new PaiementResponse();
      // Handle payment
      if (savedCommande.modePaiement().equals(ModePaiement.CARTE_BANCAIRE)) {
        // Payment logic commented out
        paiementResponse = createCommandeOnline(commandeRequest.getToken(), savedCommande);
      } else {
        paiementResponse.setPaymentRef(savedCommande.id().toString());
      }
      processOrder(savedCommande, commandeRequest.getUtilisateurId());
      return paiementResponse;
    } catch (BadRequestException | NotFoundException | UnauthorizedException
        | ConflictException e) {
      log.warn("Commande creation failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Commande creation failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  @Transactional
  public PaiementResponse createCommandeAdmin(CommandeAdminRequest commandeAdminRequest) {
    try {
      if (commandeAdminRequest == null || commandeAdminRequest.getToken() == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_request");
      }

      utilisateurRepo.findById(commandeAdminRequest.getUtilisateurId());
      log.info("commandeRequest : {}", commandeAdminRequest);

      Commande commande = commandeMapper.dtoToModel(commandeAdminRequest);
      log.info("commande: {}", commande);

      // Handle code promo
      if (commande.codePromo() != null && commande.codePromo().code() != null) {
        log.info("codePromo: {}", commande.codePromo());
        CodePromo codePromo = codePromoRepo.findByCode(commande.codePromo().code().toUpperCase());
        commande = commande.toBuilder().codePromo(codePromo).build();
      } else {
        commande = commande.toBuilder().codePromo(null).build();
      }

      commande = commande.toBuilder().statut(StatutCommande.EN_ATTENTE)
          .total(commandeAdminRequest.getTotal() + commandeAdminRequest.getShipping()).build();

      // Save commande
      Commande savedCommande = commandeRepo.save(commande);
      log.info("Commande saved with ID: {}", savedCommande.id());

      // DEBUG: Check if commande was actually saved
      if (savedCommande.id() == null) {
        log.error("CRITICAL: Commande was not saved properly - ID is null!");
        throw exceptionFactory.internalServerError("internal.server_error",
            "Failed to save commande");
      }

      // Save ligne commandes
      commandeAdminRequest.getProduits().forEach(produit -> {
        // DEBUG: Verify variant exists
        Variant existingVariant = variantRepo.findById(produit.getVariantId());
        log.info("Found variant with ID: {}", existingVariant.id());

        LigneCommande ligneCommande = LigneCommande.builder().commande(savedCommande)
            .quantite(produit.getQuantite()).variant(existingVariant)
            .prixTotal(produit.getQuantite() * produit.getNewPrice()).build();

        log.info("Attempting to save LigneCommande: {}", ligneCommande);
        LigneCommande savedLigne = ligneCommandeRepo.save(ligneCommande);
        log.info("LigneCommande saved with ID: {}", savedLigne.id());

        // DEBUG: Verify it was actually saved
        if (savedLigne.id() == null) {
          log.error("CRITICAL: LigneCommande was not saved properly!");
        }
      });
      processOrder(savedCommande, commandeAdminRequest.getUtilisateurId());

      PaiementResponse paiementResponse = new PaiementResponse();
      paiementResponse.setPaymentRef(savedCommande.id().toString());
      return paiementResponse;

    } catch (BadRequestException | NotFoundException e) {
      log.warn("Commande creation failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Commande creation failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  private PaiementResponse createCommandeOnline(String token, Commande savedCommande) {
    try {
      // Handle payment
      PaiementResponse paiementResponse;
      // Payment logic commented out
      PaiementRequest paiementRequest =
          paymentService.createPaiementRequest(savedCommande.id().toString(),
              savedCommande.total() * 1000, token, savedCommande.prenom(), savedCommande.nom(),
              savedCommande.telephone(), savedCommande.email());
      log.info("paiementRequest: {}", paiementRequest);

      paiementResponse = paymentService.initPayment(paiementRequest);
      WebhookResponse webhookResponse = paymentApi.getPaiement(paiementResponse.getPaymentRef());
      paiementRepo.save(
          Paiement.builder().commande(savedCommande).paiementRef(paiementResponse.getPaymentRef())
              .status(webhookResponse.getPayment().getStatus())
              .nom(webhookResponse.getPayment().getPaymentDetails().getName())
              .type(webhookResponse.getPayment().getType())
              .amount(webhookResponse.getPayment().getAmount())
              .date(DateMapper.INSTANCE
                  .mapOffsetDateTimeToLocalDateTime(webhookResponse.getPayment().getCreatedAt()))
              .build());
      return paiementResponse;
    } catch (BadRequestException | NotFoundException e) {
      log.warn("Commande creation failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Commande creation failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  /**
   * Retrieve an order by ID
   *
   * @param id The order ID to retrieve
   * @return The order
   */
  @Transactional
  public CommandeResponse getCommandeById(Long id) {
    try {
      if (id == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }
      Commande commande = commandeRepo.findById(id);
      // DEBUG: Check ligne commandes
      log.info("Commande {} has {} ligne commandes", id,
          commande.lignesCommande() != null ? commande.lignesCommande().size() : 0);

      return commandeMapper.modelToDto(commande);

    } catch (BadRequestException | NotFoundException | UnauthorizedException e) {
      log.warn("Commande retrieval failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Commande retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  /**
   * Retrieve an order by Ref
   *
   * @param ref The order ID to retrieve
   * @return The order
   */
  @Transactional
  public CommandeResponse getCommandeByPaymentRef(String ref) {
    try {
      if (ref == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      }
      Paiement paiement = paiementRepo.findByReference(ref);
      Commande commande = commandeRepo.findById(paiement.commande().id());
      return commandeMapper.modelToDto(commande);

    } catch (BadRequestException | NotFoundException | UnauthorizedException e) {
      log.warn("Commande retrieval failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Commande retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  /**
   * Retrieve all orders
   *
   * @return List of all orders
   */
  @Transactional
  public List<CommandeResponse> getAllCommandes() {
    try {
      log.info("retrieving all commandes models{}", commandeRepo.findAll());
      List<Commande> commandes = commandeRepo.findAll();
      return commandes.stream().map(commandeMapper::modelToDto).toList();
    } catch (NotFoundException e) {
      log.warn("No commandes found: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Commande list retrieval failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  /**
   * Retrieve orders by user ID
   *
   * @param utilisateurId The user ID to retrieve orders for
   * @return List of orders for the user
   */
  @Transactional
  public List<CommandeResponse> getCommandesByUtilisateurId(Long utilisateurId) {
    try {
      if (utilisateurId == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      }

      utilisateurRepo.findById(utilisateurId);
      List<Commande> commandes = commandeRepo.findByUserId(utilisateurId);
      return commandes.stream().map(commandeMapper::modelToDto).toList();

    } catch (BadRequestException | NotFoundException e) {
      log.warn("Commande retrieval by user failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Commande retrieval by user failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public GetFilteredCommandesPage200Response getFilteredCommandesPage(Integer page, Integer size,
      StatusCommandeEnum status, String email, Boolean orderByCreationDateDesc) {
    try {
      page = Math.max(page - 1, 0);
      size = Math.max(size, 1);
      CommandeFilter filter = CommandeFilter.builder()
          .status((status == null || status.getValue().equals("ALL")) ? null : checkStatus(status))
          .email(email).orderByCreationDateDesc(orderByCreationDateDesc).build();
      Pageable pageable = PageRequest.of(page, size,
          orderByCreationDateDesc ? Sort.by("date").descending() : Sort.by("date").ascending());
      Page<Commande> commandePage = commandeRepo.findAll(filter, pageable);
      GetFilteredCommandesPage200Response response = new GetFilteredCommandesPage200Response();
      log.info("commande content: {}", commandePage.getContent());
      log.info("commande content after mapping: {}", commandePage.getContent().stream()
          .map(commandeMapper::modelToDto).collect(Collectors.toList()));
      response.setContent(commandePage.getContent().stream().map(commandeMapper::modelToDto)
          .collect(Collectors.toList()));
      response.setTotalElements(commandePage.getTotalElements());
      response.setTotalPages(commandePage.getTotalPages());
      response.setCurrentPage(commandePage.getNumber() + 1);
      if (status == null || status.getValue().equals("ALL"))
        response.setTotalAmmount(commandeRepo.findTotalCommandes());
      else
        response.setTotalAmmount(commandeRepo.findTotalCommandesByStatut(checkStatus(status)));
      response.setGlobalTotalElements(commandeRepo.countCommandes());
      List<Commande> commandesThisWeek =
          commandeRepo.findByDateBetween(LocalDateTime.now().minusWeeks(1), LocalDateTime.now());
      log.info("commandesThisWeek: {}", LocalDateTime.now().minusWeeks(1));
      log.info("commandesThisWeek: {}", LocalDateTime.now());
      log.info("commandesThisWeek: {}", commandesThisWeek);
      List<Commande> commandesLastWeek = commandeRepo
          .findByDateBetween(LocalDateTime.now().minusWeeks(2), LocalDateTime.now().minusWeeks(1));
      log.info("commandesLastWeek: {}", LocalDateTime.now().minusWeeks(2));
      log.info("commandesLastWeek: {}", LocalDateTime.now().minusWeeks(1));
      log.info("commandesLastWeek: {}", commandesLastWeek);
      response.setCurrentWeekOrders((long) commandesThisWeek.size());
      response.setLastWeekOrders((long) commandesLastWeek.size());
      response.setCurrentWeekOrdersTotalAmount(
          commandesThisWeek.stream().mapToDouble(Commande::total).sum());
      response.setLastWeekOrdersTotalAmount(
          commandesLastWeek.stream().mapToDouble(Commande::total).sum());
      return response;
    } catch (BadRequestException e) {
      log.warn("Commande retrieval by user failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Commande retrieval by user failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }

  }

  /**
   * Update the status of an order by ID
   *
   * @param id The order ID to update
   * @param commandeRequest The new status of the order
   * @return The updated order
   */
  @Transactional
  public CommandeResponse updateStatusCommande(Long id,
      UpdateCommandeStatusRequest commandeRequest) {
    try {
      if (id == null || commandeRequest == null || commandeRequest.getStatus() == null
          || commandeRequest.getStatus().getValue() == null
          || commandeRequest.getStatus().getValue().isEmpty()) {
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      }

      Commande oldCommande = commandeRepo.findById(id);
      StatutCommande updatedStatus = checkStatus(commandeRequest.getStatus());

      Commande newCommande = oldCommande.toBuilder().id(id).statut(updatedStatus).build();

      newCommande = commandeRepo.save(newCommande);
      updateStock(oldCommande, newCommande);

      log.info("updated commande: {}", newCommande);
      emailInterface.sendOrderNotificationEmail(newCommande, true);
      return commandeMapper.modelToDto(newCommande);

    } catch (BadRequestException | NotFoundException | ConflictException e) {
      log.warn("Commande status update failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Commande status update failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  /**
   * Update CustomerDetails
   *
   * @param id The order ID to update
   * @param commandeRequest The new customer details
   * @return The updated order
   */
  @Transactional
  public CommandeResponse updateCustomerDetails(Long id,
      UpdateCommandeCustomerDetailsRequest commandeRequest) {
    try {
      if (id == null || commandeRequest == null || commandeRequest.getEmail() == null
          || commandeRequest.getNom() == null || commandeRequest.getPrenom() == null
          || commandeRequest.getTelephone() == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      }

      Commande commande = commandeRepo.findById(id);
      commande = commande.toBuilder().email(commandeRequest.getEmail())
          .nom(commandeRequest.getNom()).prenom(commandeRequest.getPrenom())
          .telephone(commandeRequest.getTelephone()).build();

      Commande updatedCommande = commandeRepo.save(commande);
      return commandeMapper.modelToDto(updatedCommande);

    } catch (BadRequestException | NotFoundException | ConflictException e) {
      log.warn("Commande customer details update failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Commande customer details update failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  /**
   * Delete an order by ID
   *
   * @param id The order ID to delete
   */
  @Transactional
  public void deleteCommande(Long id) {
    try {
      if (id == null) {
        throw exceptionFactory.badRequest("badrequest.invalid_id");
      }

      Commande commande = commandeRepo.findById(id);
      if (isRefundable(commande.statut())) {
        stockService.updateStockForCommande(commande, true);
      }
      commandeRepo.deleteById(id);

    } catch (BadRequestException | NotFoundException e) {
      log.warn("Commande deletion failed with client error: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Commande deletion failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  /**
   * Delete all orders
   */
  @Transactional
  public void deleteAllCommandes() {
    try {
      commandeRepo.findAll().forEach(commande -> {
        if (isRefundable(commande.statut())) {
          stockService.updateStockForCommande(commande, true);
        }
      });
      commandeRepo.deleteAll();
    } catch (Exception e) {
      log.error("Commande deletion failed unexpectedly", e);
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }


  private StatutCommande checkStatus(StatusCommandeEnum statut) {
    if (statut == null || statut.getValue() == null || statut.getValue().isBlank()) {
      throw exceptionFactory.badRequest("badrequest.invalid_input");
    }
    for (StatutCommande status : StatutCommande.values()) {
      if (status.name().equals(statut.getValue())) {
        return status;
      }
    }
    throw exceptionFactory.badRequest("badrequest.invalid_input");
  }


  private boolean isActive(StatutCommande statut) {
    return statut == StatutCommande.EN_COURS || statut == StatutCommande.EXPEDIEE
        || statut == StatutCommande.EN_COURS_PREPARATION || statut == StatutCommande.EN_ATTENTE
        || statut == StatutCommande.LIVREE || statut == StatutCommande.CONFIRMEE
        || statut == StatutCommande.TERMINEE;
  }

  private boolean isRefundable(StatutCommande statut) {
    return statut == StatutCommande.EN_COURS || statut == StatutCommande.EXPEDIEE
        || statut == StatutCommande.EN_COURS_PREPARATION || statut == StatutCommande.EN_ATTENTE
        || statut == StatutCommande.LIVREE || statut == StatutCommande.CONFIRMEE;
  }

  private boolean isTerminated(StatutCommande statut) {
    return statut == StatutCommande.TERMINEE;
  }

  private boolean isReturnedOrCancelled(StatutCommande statut) {
    return statut == StatutCommande.ANNULEE || statut == StatutCommande.RETOUR;
  }

  private boolean isCancelled(StatutCommande statut) {
    return statut == StatutCommande.ANNULEE;
  }

  private boolean isReturned(StatutCommande statut) {
    return statut == StatutCommande.RETOUR;
  }

  private void updateStock(Commande oldCommande, Commande newCommande) throws ConflictException {
    if (isCancelled(oldCommande.statut()))
      throw exceptionFactory.conflict("conflict.commande_cancelled");
    if (isTerminated(oldCommande.statut()))
      throw exceptionFactory.conflict("conflict.commande_terminated");
    if (isActive(oldCommande.statut()) && isReturnedOrCancelled(newCommande.statut())) {
      stockService.updateStockForCommande(newCommande, true); // restore stock
    }

    if (isReturned(oldCommande.statut()) && isActive(newCommande.statut())) {
      stockService.updateStockForCommande(newCommande, false); // reduce again
    }
  }

  private void processOrder(Commande commande, Long userId)
      throws MessagingException, TemplateException, IOException, ConflictException {
    // Manually build commande with lines since lazy loading won't work
    List<LigneCommande> savedLines = ligneCommandeRepo.findByCommandeId(commande.id());
    log.info("Total LigneCommandes saved for commande {}: {}", commande.id(), savedLines.size());
    Commande commandeWithLines = commande.toBuilder().lignesCommande(savedLines).build();

    // Process order
    stockService.updateStockForCommande(commandeWithLines, false);
    try {
      utilisateurRepo.findById(userId);
      notifService.notifyOrder(commandeWithLines);
    } catch (NotFoundException ignore) {
    }
    factureRepo.save(
        Facture.builder().commande(commandeWithLines).total(commandeWithLines.total()).build());
    emailInterface.sendOrderNotificationEmail(commandeWithLines, false);
    emailInterface.sendOrderCreatedToAdmins(commandeWithLines);
  }

  // verif commandes owner
  public boolean isConnectedUserCommandeOwner(Long userId) throws UnauthorizedException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
      throw exceptionFactory.unauthorized("unauthorized.not_authenticated");
    }
    return userId.equals(userDetails.id());
  }

  public boolean isCommandeOwner(Long orderId, Long userId)
      throws UnauthorizedException, NotFoundException {
    commandeRepo.findById(orderId);
    if (userId == null) {
      UtilisateurAnonymeResponse response = utilisateurAnonymeService
          .getOrCreateUtilisateurAnonyme(cookiesUtil.getUUIDCookieValue("anonyme_session_token"));
      return commandeRepo.existsByIdAndUser_IdAndUserType(orderId, response.getId(),
          UserType.ANONYMOUS);
    }
    utilisateurRepo.findById(userId);
    return commandeRepo.existsByIdAndUser_IdAndUserType(orderId, userId, UserType.NORMAL);
  }

  public boolean isCommandeOwnerByRef(String ref, Long userId)
      throws UnauthorizedException, NotFoundException {
    Paiement paiement = paiementRepo.findByReference(ref);
    if (paiement == null) {
      throw exceptionFactory.notFound("notfound.paiement");
    }
    if (paiement.commande() == null || paiement.commande().id() == null)
      throw exceptionFactory.notFound("notfound.commande");
    Commande commande = commandeRepo.findById(paiement.commande().id());
    return isCommandeOwner(commande.id(), userId);
  }

  public boolean isAdmin(Long userId) {
    if (userId != null) {
      UtilisateurModel user = utilisateurRepo.findById(userId);
      return user.roles().contains(Role.ADMIN);
    }
    return false;
  }



}
