
package tn.temporise.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import tn.temporise.application.service.PaymentService;
import tn.temporise.application.service.WebhookService;
import tn.temporise.domain.model.PaiementRequest;
import tn.temporise.domain.model.PaiementResponse;
import tn.temporise.domain.model.WebhookResponse;
import tn.temporise.infrastructure.api.PaiementsApi;
import tn.temporise.infrastructure.client.payment.PaymentApi;


@RestController
@RequiredArgsConstructor
@Slf4j
public class PaiementController implements PaiementsApi {
  private final PaymentService paymentService;
  private final WebhookService webhookService;
  private final PaymentApi paymentApi;

  @Override
  public ResponseEntity<PaiementResponse> _createPaiement(PaiementRequest paiementRequest)
      throws Exception {
    PaiementResponse response = paymentService.initPayment(paiementRequest);
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<WebhookResponse> _getKonnectWebhook(String paymentRef) throws Exception {
    WebhookResponse response = webhookService.getPayment(paymentRef);
    return ResponseEntity.ok(response);
  }

  @PreAuthorize("@paymentService.isPaymentDetailsOwnerByRef(#paymentRef,#userId)")
  @Override
  public ResponseEntity<WebhookResponse> _getPaymentDetails(String paymentRef, Long userId)
      throws Exception {
    return ResponseEntity.ok(paymentApi.getPaiement(paymentRef));
  }

}
