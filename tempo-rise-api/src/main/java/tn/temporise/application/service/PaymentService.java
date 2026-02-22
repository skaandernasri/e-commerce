package tn.temporise.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.auth.UnauthorizedException;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.CommandeRepo;
import tn.temporise.domain.port.PaiementRepo;
import tn.temporise.domain.port.UserRepo;
import tn.temporise.infrastructure.client.payment.PaymentApi;
import tn.temporise.infrastructure.security.utils.CookiesUtil;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class PaymentService {
  private final PaymentApi paymentApi;
  private final ExceptionFactory exceptionFactory;
  private final PaiementRepo paiementRepo;
  private final CommandeRepo commandeRepo;
  private final UtilisateurAnonymeService utilisateurAnonymeService;
  private final CookiesUtil cookiesUtil;
  private final UserRepo utilisateurRepo;
  @Value("${konnect.key}")
  private String x_api_key;
  @Value("${konnect.receiverWalletId}")
  private String receiverWalletId;

  public PaiementResponse initPayment(PaiementRequest request) {
    try {
      if (request == null || request.getReceiverWalletId() == null || request.getAmount() == null)
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      return paymentApi.createPaiement(x_api_key, request);
    } catch (BadRequestException e) {
      throw e;
    } catch (Exception e) {
      log.info("exeption in payment service error: {}", e.getMessage());
      log.info("contaiçns 422? {}", e.getMessage().contains("422"));
      if (e.getMessage().contains("404"))
        throw exceptionFactory.paymentException("external.payment_not_found", e.getMessage());
      if (e.getMessage().contains("500"))
        throw exceptionFactory.paymentException("external.payment_failed", e.getMessage());
      if (e.getMessage().contains("401"))
        throw exceptionFactory.paymentException("external.payment_unauthorized", e.getMessage());
      if (e.getMessage().contains("422"))
        throw exceptionFactory.paymentException("external.payment_unprocessable", e.getMessage());

      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public boolean isPaymentOwnerByReference(String payRef) throws NotFoundException {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null
        || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {

      throw exceptionFactory.unauthorized("unauthorized.not_authenticated");
    }
    log.info("user id: {}", userDetails.id());
    log.info("payRef: {}", payRef);
    log.info("isPaymentOwnerByReference: {}",
        paiementRepo.existsByReferenceAndUserId(payRef, userDetails.id()));
    return paiementRepo.existsByReferenceAndUserId(payRef, userDetails.id());
  }

  public PaiementRequest createPaiementRequest(String commandeId, Double amount, String token,
      String prenom, String nom, String telephone, String email) {
    PaiementRequest paiementRequest = new PaiementRequest();
    log.info("paiment request");
    paiementRequest.setReceiverWalletId(receiverWalletId);
    paiementRequest.setToken(token);
    paiementRequest.setAmount((double) Math.round(amount));
    paiementRequest.setType(PaiementRequest.TypeEnum.IMMEDIATE);
    paiementRequest.setDescription("description");
    paiementRequest
        .setAcceptedPaymentMethods(List.of(PaiementRequest.AcceptedPaymentMethodsEnum.WALLET,
            PaiementRequest.AcceptedPaymentMethodsEnum.BANK_CARD,
            PaiementRequest.AcceptedPaymentMethodsEnum.E_DINAR));
    paiementRequest.setLifespan(10);
    paiementRequest.setFirstName(prenom);
    paiementRequest.setLastName(nom);
    paiementRequest.setPhoneNumber(telephone);
    paiementRequest.setEmail(email);
    paiementRequest.setOrderId(commandeId);
    paiementRequest.setWebhook(URI.create("https://api.temposphere.tn/v1/konnect/webhook"));
    paiementRequest.setSilentWebhook(true);
    paiementRequest.setTheme(PaiementRequest.ThemeEnum.DARK);
    return paiementRequest;
  }

  public boolean isPaymentDetailsOwnerByRef(String ref, Long userId)
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

  private boolean isCommandeOwner(Long orderId, Long userId)
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
}
