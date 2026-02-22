package tn.temporise.application.service;

import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.temporise.application.exception.client.NotFoundException;
import tn.temporise.config.ExceptionFactory;
import tn.temporise.domain.model.CodePromo;
import tn.temporise.domain.model.SendEmailRequest;
import tn.temporise.domain.model.UtilisateurModel;
import tn.temporise.domain.model.WebhookResponsePayment;
import tn.temporise.domain.port.CodePromoRepo;
import tn.temporise.domain.port.EmailInterface;
import tn.temporise.domain.port.UserRepo;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
  private final EmailInterface emailInterface;
  private final ExceptionFactory exceptionFactory;
  private final UserRepo userRepo;
  private final NotifService notifService;
  private final CodePromoRepo codePromoRepo;

  public void sendVerifEmail(SendEmailRequest sendEmailRequest) {
    try {
      emailInterface.sendVerificationEmail(sendEmailRequest.getTo());
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public void sendResetPasswordEmail(SendEmailRequest sendEmailRequest) {
    try {
      emailInterface.sendRestPasswordConfirmationEmail(sendEmailRequest.getTo());
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public void activate(String token) {
    try {
      UtilisateurModel user = userRepo.findByActivationToken(token);
      user = user.toBuilder().isverified(true).activation_token(null).activationtokenexpiresat(null)
          .build();
      userRepo.save(user);
    } catch (NotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public void sendPaymentStatusEmail(WebhookResponsePayment payment)
      throws MessagingException, IOException, TemplateException {
    try {
      emailInterface.sendPaymentStatusEmail(payment);
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }

  public void sendPromoCodeEmail(SendEmailRequest sendEmailRequest) {
    try {
      if (sendEmailRequest.getTo() == null || sendEmailRequest.getTo().isEmpty()
          || sendEmailRequest.getCodePromo() == null || sendEmailRequest.getCodePromo().isEmpty()) {
        throw exceptionFactory.badRequest("badrequest.invalid_request");
      }
      UtilisateurModel user = userRepo.findByEmail(sendEmailRequest.getTo());
      CodePromo codePromo1 = codePromoRepo.findByCode(sendEmailRequest.getCodePromo());
      notifService.notifyCodePromo(user, codePromo1);
      emailInterface.sendPromoCodeEmail(sendEmailRequest.getTo(), sendEmailRequest.getSubject(),
          sendEmailRequest.getText(), codePromo1);
    } catch (NotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw exceptionFactory.internalServerError("internal.server_error", e.getMessage());
    }
  }
}
