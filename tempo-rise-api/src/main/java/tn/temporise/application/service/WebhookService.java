package tn.temporise.application.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.application.exception.external.PaymentException;
import tn.temporise.application.mapper.DateMapper;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.CommandeRepo;
import tn.temporise.domain.port.PaiementRepo;
import tn.temporise.domain.port.UserRepo;
import tn.temporise.infrastructure.client.payment.PaymentApi;


import java.util.Objects;

@RequiredArgsConstructor
@Slf4j
@Service
public class WebhookService {
  private final PaymentApi paymentApi;
  private final ExceptionFactory exceptionFactory;
  private final CommandeRepo commandeRepo;
  private final EmailService emailService;
  private final NotifService notifService;
  private final PaiementRepo paiementRepo;
  private final UserRepo utilisateurRepo;
  private final StockService stockService;

  @Transactional
  public WebhookResponse getPayment(String id) {
    try {
      if (id == null || id.isBlank())
        throw exceptionFactory.badRequest("badrequest.invalid_input");
      WebhookResponse webhookResponse = paymentApi.getPaiement(id);
      Commande commande =
          commandeRepo.findById(Long.parseLong(webhookResponse.getPayment().getOrderId()));
      if (Objects.requireNonNull(
          webhookResponse.getPayment().getStatus()) == WebhookResponsePayment.StatusEnum.COMPLETED
          || Objects.requireNonNull(webhookResponse.getPayment()
              .getStatus()) == WebhookResponsePayment.StatusEnum.PENDING) {
        commande = commande.toBuilder().statut(StatutCommande.EN_COURS).build();
      } else {
        commande = commande.toBuilder().statut(StatutCommande.ANNULEE).build();
        stockService.updateStockForCommande(commande, true);
      }
      Commande savedCommande = commandeRepo.save(commande);
      Paiement savedPaiement = paiementRepo.save(Paiement.builder().commande(savedCommande)
          .paiementRef(id).status(webhookResponse.getPayment().getStatus())
          .nom(webhookResponse.getPayment().getPaymentDetails().getName())
          .type(webhookResponse.getPayment().getType())
          .amount(webhookResponse.getPayment().getAmount())
          .date(DateMapper.INSTANCE
              .mapOffsetDateTimeToLocalDateTime(webhookResponse.getPayment().getCreatedAt()))
          .build());
      log.info("paiement saved: {}", savedPaiement);
      try {
        utilisateurRepo.findById(commande.user().id());
        notifService.notifyOrder(commande);
      } catch (NotFoundException ignore) {
      }
      emailService.sendPaymentStatusEmail(webhookResponse.getPayment());
      return webhookResponse;
    } catch (NotFoundException | BadRequestException | PaymentException e) {
      throw e;
    } catch (Exception e) {
      log.info("exeption in payment service error: {}", e.getMessage());
      log.info("contaiçns 422? {}", e.getMessage().contains("422"));
      if (e.getMessage().contains("404"))
        throw exceptionFactory.paymentException("external.payment_not_found", e.getMessage());
      else if (e.getMessage().contains("500"))
        throw exceptionFactory.paymentException("external.payment_failed", e.getMessage());
      else if (e.getMessage().contains("401"))
        throw exceptionFactory.paymentException("external.payment_unauthorized", e.getMessage());
      else if (e.getMessage().contains("422"))
        throw exceptionFactory.paymentException("external.payment_unprocessable", e.getMessage());
      else
        throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }
}
