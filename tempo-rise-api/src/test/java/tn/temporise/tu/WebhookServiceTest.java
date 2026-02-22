package tn.temporise.tu;

import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import tn.temporise.application.exception.client.BadRequestException;
import tn.temporise.application.exception.external.PaymentException;
import tn.temporise.application.service.EmailService;
import tn.temporise.application.service.NotifService;
import tn.temporise.application.service.StockService;
import tn.temporise.application.service.WebhookService;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.*;
import tn.temporise.domain.port.CommandeRepo;
import tn.temporise.domain.port.PaiementRepo;
import tn.temporise.domain.port.UserRepo;
import tn.temporise.domain.port.VariantRepo;
import tn.temporise.infrastructure.client.payment.PaymentApi;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WebhookServiceTest {

  @InjectMocks
  private WebhookService webhookService;

  @Mock
  private PaymentApi paymentApi;
  @Mock
  private ExceptionFactory exceptionFactory;
  @Mock
  private CommandeRepo commandeRepo;
  @Mock
  private VariantRepo variantRepo;
  @Mock
  private EmailService emailService;
  @Mock
  private NotifService notifService;
  @Mock
  private PaiementRepo paiementRepo;
  @Mock
  private StockService stockService;
  @Mock
  private UserRepo utilisateurRepo;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testGetPaymentCompleted_success() throws MessagingException, TemplateException, IOException {
    // Given
    UtilisateurModel user = UtilisateurModel.builder().id(1L).build();

    String paymentId = "payment123";

    WebhookResponsePaymentPaymentDetails paymentDetails =
        new WebhookResponsePaymentPaymentDetails();
    paymentDetails.setName("John Doe");

    WebhookResponsePayment payment = new WebhookResponsePayment();
    payment.setOrderId("1");
    payment.setStatus(WebhookResponsePayment.StatusEnum.COMPLETED);
    payment.setAmount(BigDecimal.valueOf(100.00));
    payment.setCreatedAt(OffsetDateTime.now());
    payment.setType("CARD");
    payment.setPaymentDetails(paymentDetails);

    WebhookResponse response = new WebhookResponse();
    response.setPayment(payment);

    Variant variant = Variant.builder().id(10L).quantity(10L).build();
    LigneCommande ligneCommande = LigneCommande.builder().quantite(2).variant(variant).build();

    Commande commande = Commande.builder().id(1L).statut(StatutCommande.EN_COURS)
        .lignesCommande(List.of(ligneCommande)).user(user).build();

    Variant updatedVariant = Variant.builder().id(10L).quantity(8L).build();
    Commande updatedCommande =
        commande.toBuilder().statut(StatutCommande.EN_COURS).user(user).build();
    Paiement savedPaiement = Paiement.builder().id(99L).commande(updatedCommande).build();

    // When
    when(paymentApi.getPaiement(paymentId)).thenReturn(response);
    when(commandeRepo.findById(1L)).thenReturn(commande);
    when(variantRepo.findById(10L)).thenReturn(variant);
    when(variantRepo.save(any())).thenReturn(updatedVariant);
    when(commandeRepo.save(any())).thenReturn(updatedCommande);
    when(paiementRepo.save(any())).thenReturn(savedPaiement);
    when(utilisateurRepo.findById(1L)).thenReturn(user);

    // Act
    WebhookResponse result = webhookService.getPayment(paymentId);

    // Then
    assertNotNull(result);
    assertEquals(WebhookResponsePayment.StatusEnum.COMPLETED, result.getPayment().getStatus());

    verify(paymentApi).getPaiement(paymentId);
    verify(commandeRepo).findById(1L);
    verify(commandeRepo).save(argThat(cmd -> cmd.statut() == StatutCommande.EN_COURS));
    verify(paiementRepo).save(any(Paiement.class));
    verify(emailService).sendPaymentStatusEmail(payment);
    verify(notifService).notifyOrder(argThat(cmd -> cmd.statut() == StatutCommande.EN_COURS));
    verifyNoMoreInteractions(paymentApi, commandeRepo, variantRepo, paiementRepo, emailService,
        notifService);
  }


  @Test
  void testGetPayment_invalidId_throwsBadRequest() {
    // GIVEN
    String id = " ";
    when(exceptionFactory.badRequest(any()))
        .thenReturn(new BadRequestException("badrequest.invalid_input", "4000"));

    // WHEN / THEN
    assertThrows(BadRequestException.class, () -> webhookService.getPayment(id));
  }

  @Test
  void testGetPayment_handlesKnownErrors() {
    // GIVEN
    String id = "paymentXYZ";
    when(paymentApi.getPaiement(id)).thenThrow(new RuntimeException("500 Internal Error"));
    when(exceptionFactory.paymentException(eq("external.payment_failed"), any()))
        .thenReturn(new PaymentException("external.payment_failed", "5000"));

    // WHEN / THEN
    assertThrows(PaymentException.class, () -> webhookService.getPayment(id));
  }

}
