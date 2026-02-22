package tn.temporise.ti;

import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tn.temporise.application.service.EmailService;
import tn.temporise.application.service.NotifService;
import tn.temporise.application.service.WebhookService;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.CommandeRepo;
import tn.temporise.domain.port.PaiementRepo;
import tn.temporise.domain.port.VariantRepo;
import tn.temporise.infrastructure.client.payment.PaymentApi;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class WebhookServiceIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private WebhookService webhookService;

  @MockitoBean
  private PaymentApi paymentApi;

  @MockitoBean
  private CommandeRepo commandeRepo;

  @MockitoBean
  private VariantRepo variantRepo;

  @MockitoBean
  private EmailService emailService;

  @MockitoBean
  private NotifService notifService;

  @MockitoBean
  private PaiementRepo paiementRepo;

  @Test
  void testPaymentCompletedFlow() throws MessagingException, TemplateException, IOException {
    // Arrange
    UtilisateurModel user = UtilisateurModel.builder().id(userId).build();
    String paymentId = "pay_456";

    // Variant and LigneCommande mock
    Variant variant = Variant.builder().id(1L).quantity(5L).build();
    LigneCommande ligneCommande = LigneCommande.builder().quantite(1).variant(variant).build();
    Commande commande =
        Commande.builder().id(1L).lignesCommande(List.of(ligneCommande)).user(user).build();

    // Mocked webhook response
    WebhookResponsePayment payment = new WebhookResponsePayment();
    payment.setOrderId("1");
    payment.setStatus(WebhookResponsePayment.StatusEnum.COMPLETED);
    payment.setType("CARD");
    payment.setAmount(BigDecimal.valueOf(100.00));
    payment.setCreatedAt(OffsetDateTime.now());

    WebhookResponsePaymentPaymentDetails paymentDetails =
        new WebhookResponsePaymentPaymentDetails();
    paymentDetails.setName("John Doe");
    payment.setPaymentDetails(paymentDetails);

    WebhookResponse webhookResponse = new WebhookResponse();
    webhookResponse.setPayment(payment);

    // Define mock behavior
    when(paymentApi.getPaiement(paymentId)).thenReturn(webhookResponse);
    when(commandeRepo.findById(1L)).thenReturn(commande);
    when(variantRepo.findById(1L)).thenReturn(variant);
    when(commandeRepo.save(any(Commande.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(paiementRepo.save(any(Paiement.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    // when(userRepo.findById(userId)).thenReturn(user);

    // Act
    WebhookResponse result = webhookService.getPayment(paymentId);

    // Assert
    assertEquals(WebhookResponsePayment.StatusEnum.COMPLETED, result.getPayment().getStatus());
    verify(commandeRepo).save(any(Commande.class));
    // verify(notifService).notifyOrder(any(Commande.class));
    verify(emailService).sendPaymentStatusEmail(payment);
    verify(paiementRepo).save(any(Paiement.class));
  }
}
