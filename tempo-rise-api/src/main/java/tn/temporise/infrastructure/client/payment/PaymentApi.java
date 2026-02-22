package tn.temporise.infrastructure.client.payment;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import tn.temporise.domain.model.PaiementRequest;
import tn.temporise.domain.model.PaiementResponse;
import tn.temporise.domain.model.WebhookResponse;

@FeignClient(name = "konnectClient", url = "https://api.konnect.network")
public interface PaymentApi {
  @PostMapping(value = "/api/v2/payments/init-payment", consumes = "application/json",
      produces = "application/json")
  PaiementResponse createPaiement(@RequestHeader("x-api-key") String xApiKey,
      @RequestBody PaiementRequest request);

  @GetMapping(value = "/api/v2/payments/{paymentId}")
  WebhookResponse getPaiement(@PathVariable("paymentId") String paymentId);
}

